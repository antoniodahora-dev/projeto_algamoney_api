package com.example.algamoneyapi.service;

import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.algamoneyapi.dto.LancamentoEstatisticasPessoa;
import com.example.algamoneyapi.mail.Mailer;
import com.example.algamoneyapi.model.Lancamento;
import com.example.algamoneyapi.model.Pessoa;
import com.example.algamoneyapi.model.Usuario;
import com.example.algamoneyapi.repository.LancamentoRepository;
import com.example.algamoneyapi.repository.PessoaRepository;
import com.example.algamoneyapi.repository.UsuarioRepository;
import com.example.algamoneyapi.service.exception.PessoaInexistenteOuInativaException;
import com.example.algamoneyapi.storage.S3;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Service
public class LancamentoService {
	
	private static final String DESTINATARIOS = "ROLE_PESQUISAR_LANCAMENTO";
	
	private static final Logger logger = LoggerFactory.getLogger(LancamentoService.class);
	
	@Autowired
	private LancamentoRepository lancamentoRepository;
	
	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PessoaRepository pessoaRepository;

	@Autowired
	private Mailer mailer;
	
	@Autowired
	private S3 s3;
	
	
	
	//irá configurar o SCHEDULED para pega o horario referente a time zone
	private static final String TIME_ZONE = "America/Sao_Paulo";
	
	//Metodo que agenda o envio de email automaticamente
	//@Scheduled(fixedDelay = 1000 * 60 * 30) // executa o envio de email de forma fixa
	@Scheduled(cron = "0 0 20 * * * ", zone = TIME_ZONE) // enviara o email programado por uma determinada hora ou dia ou mes
	public void avisarSobreLancamentoVencidos() {
		
		if (logger.isDebugEnabled()) {
			logger.debug("Preparando o envio de "
					+ "emails de aviso de Lancamentos Vencidos.");
		}
		
		
		List<Lancamento> vencidos = lancamentoRepository.findByDataVencimentoLessThanEqualAndDatapagamentoIsNull(LocalDate.now());
		
		if (vencidos.isEmpty()) {
			logger.info("Sem Lançamentos vencidos para aviso");
			
			return;			
		}
		
		logger.info(" Exitem {} Lançamentos vencidos" , vencidos.size());
		
		List<Usuario> destinatarios = usuarioRepository.findByPermissoesDescricao(DESTINATARIOS);
		
		if (destinatarios.isEmpty()) {
			logger.warn("Existem lançamentos vencidos, mas o"
					+ "sistema não encontrou destinatários"); 
			
			return;
		}
		
		mailer.avisarSobreLancamentosVencidos(vencidos, destinatarios);
		
		logger.info("Envio de email de aviso concluído");
		
		//System.out.println(">>>>>>>>>> Metodo Enviado");
	}
	
	
	
	/*Tratamento para salvar um lancamento. Irá salvar um novo lançamento caso haja um 
	 * codigo de pessoa existente ou ativa*/ //@Valid
	public Lancamento salvar( Lancamento lancamento) {
				
		/*Optional<Pessoa> pessoa = pessoaRepository.findById(lancamento.getPessoa().getCodigo());
		if(!pessoa.isPresent() || pessoa.get().isInativo()) {
			throw new PessoaInexistenteOuInativaException();
		}*/
		
		validarPessoa(lancamento);
		
		//irá salvar o arquivo temporario permantemente na Amazon S3
		if(StringUtils.hasText(lancamento.getAnexo())) {
			s3.salvar(lancamento.getAnexo());
		}
			
		return lancamentoRepository.save(lancamento);
	}
	
	
	//metodo que ira receber os parametros para gerar o relatorio
	public byte[] relatorioPorPessoa(LocalDate inicio, LocalDate fim) throws Exception {
		
		List<LancamentoEstatisticasPessoa> dados = lancamentoRepository.porPessoa(inicio, fim);
		
		Map<String, Object> parametros = new HashMap<>();
		
		parametros.put("DT_INICIO", Date.valueOf(inicio));
		parametros.put("DT_FIM", Date.valueOf(fim));
		
		//para que campo total seja formatada na moeda brasileira
		parametros.put("REPORT_LOCALE", new Locale("pt", "BR"));
		
		InputStream	inputStream = this.getClass().
				getResourceAsStream("/relatorios/lancamentos-por-pessoa.jasper");
		
		JasperPrint jasperPrint = JasperFillManager.fillReport(inputStream, parametros, 
					new JRBeanCollectionDataSource(dados));
	
		return JasperExportManager.exportReportToPdf(jasperPrint);
		
	}
	
	
	
	//Metodo para implementar atualizacao de cadastro de Lancamento 
	public Lancamento atualizar(Long codigo, Lancamento lancamento) {
		
		Lancamento lancamentoSalvo = buscarLancamentoExistente(codigo);
		if(!lancamento.getPessoa().equals(lancamentoSalvo.getPessoa())) {
			validarPessoa(lancamento);
		}

		//ira remover o anexo caso esteja vazio
		if (StringUtils.isEmpty(lancamento.getAnexo())
			&& StringUtils.hasText(lancamentoSalvo.getAnexo()))	{
		
		s3.remover(lancamentoSalvo.getAnexo());
		
		} 
		//ira substituir o anexo
		else if(StringUtils.hasLength(lancamento.getAnexo())
				&& !lancamento.getAnexo().equals(lancamentoSalvo.getAnexo())) {
			s3.substituir(lancamentoSalvo.getAnexo(), lancamento.getAnexo());
		}
		
		//ele ira altera a informacoes do Lancamento com excecao do codigo do lancamento
		BeanUtils.copyProperties(lancamento, lancamentoSalvo, "codigo");
		
		//retorno alteracao das informacoes em Lancamento
		return lancamentoRepository.save(lancamentoSalvo);
		
	}

	//valida as informacao da pessoa que esta solicitando alteracao
	private void validarPessoa(Lancamento lancamento) {
		Optional<Pessoa> pessoa = null;
		if (lancamento.getPessoa().getCodigo()!= null) {
			pessoa = pessoaRepository.findById(lancamento.getPessoa().getCodigo());
		}
		
		if (!pessoa.isPresent()) { // isPresent() verifica se há um objeto pessoa
			throw new PessoaInexistenteOuInativaException(); // se não houver lanca uma exceçao
		}

	}

	private Lancamento buscarLancamentoExistente(Long codigo) {
	
		Optional<Lancamento> lancamentoSalvo = lancamentoRepository.findById(codigo);
		if (lancamentoSalvo == null) {
			throw new IllegalArgumentException();
			
		}
		
		// se o valor estiver presente, retorna o valor , senao lanca uma excecao
		return lancamentoSalvo.orElseThrow(() -> new IllegalArgumentException());
	}


}
