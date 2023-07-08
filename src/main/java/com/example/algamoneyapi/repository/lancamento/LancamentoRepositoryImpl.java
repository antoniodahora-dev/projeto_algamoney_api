package com.example.algamoneyapi.repository.lancamento;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import com.example.algamoneyapi.dto.LancamentoEstatisticaCategoria;
import com.example.algamoneyapi.dto.LancamentoEstatisticaDia;
import com.example.algamoneyapi.dto.LancamentoEstatisticasPessoa;
import com.example.algamoneyapi.model.Categoria_;
import com.example.algamoneyapi.model.Lancamento;
import com.example.algamoneyapi.model.Lancamento_;
import com.example.algamoneyapi.model.Pessoa_;
import com.example.algamoneyapi.model.TipoLancamento;
import com.example.algamoneyapi.repository.filter.LancamentoFilter;
import com.example.algamoneyapi.repository.projection.ResumoLancamento;

/*Classe para realizar a pesquisa*
 * Por período data
 * */

public class LancamentoRepositoryImpl implements LancamentoRepositoryQuery {
	
	@PersistenceContext
	private EntityManager manager;


	//ira gerar a informcao por Pessoa
		@Override
		public List<LancamentoEstatisticasPessoa> porPessoa(LocalDate inicio, LocalDate fim) {
			
			CriteriaBuilder criteriaBuilder = manager.getCriteriaBuilder();
			
			//vamos devolver para aplicacao
			CriteriaQuery<LancamentoEstatisticasPessoa> criteriaQuery = criteriaBuilder.
					createQuery(LancamentoEstatisticasPessoa.class);
			
			//onde vamos buscar os dados em Lancamento
			Root<Lancamento> root = criteriaQuery.from(Lancamento.class);
			
			criteriaQuery.select(criteriaBuilder.construct(LancamentoEstatisticasPessoa.class, 
					root.get(Lancamento_.tipo),
					root.get(Lancamento_.pessoa),
					criteriaBuilder.sum(root.get(Lancamento_.valor))));
		
		
			criteriaQuery.where(
					//vamos trabalhar a condicoes para pegar a data quando for maior ou igual ao primeiro dia
					criteriaBuilder.greaterThanOrEqualTo(root.get(Lancamento_.dataVencimento), inicio),
					
					//vamos trabalhar a condicoes para pegar a data quando for menor ou igual ao ultimo dia
					criteriaBuilder.lessThanOrEqualTo(root.get(Lancamento_.dataVencimento), fim));
			
			//vamos agrupar as informaçoes por categoria
			criteriaQuery.groupBy(root.get(Lancamento_.tipo), root.get(Lancamento_.pessoa));
			
			//ira enviar o resultado da solicitacao
			TypedQuery<LancamentoEstatisticasPessoa> typedQuery = manager.createQuery(criteriaQuery);
			
			return typedQuery.getResultList();
		}
	
	
	
	//ira gerar a informcao por dia
	@Override
	public List<LancamentoEstatisticaDia> porDia(LocalDate mesReferencia) {
		
		CriteriaBuilder criteriaBuilder = manager.getCriteriaBuilder();
		
		//vamos devolver para aplicacao
		CriteriaQuery<LancamentoEstatisticaDia> criteriaQuery = criteriaBuilder.
				createQuery(LancamentoEstatisticaDia.class);
		
		//onde vamos buscar os dados em Lancamento
		Root<Lancamento> root = criteriaQuery.from(Lancamento.class);
		
		criteriaQuery.select(criteriaBuilder.construct(LancamentoEstatisticaDia.class, 
				root.get(Lancamento_.tipo),
				root.get(Lancamento_.dataVencimento),
				criteriaBuilder.sum(root.get(Lancamento_.valor))));
		
		//ira pegar o primeiro dia do mês
		LocalDate primeiroDia = mesReferencia.withDayOfMonth(1);
		
		//ira pegar o ultimo dia do mês
		LocalDate ultimoDia = mesReferencia.withDayOfMonth(mesReferencia.lengthOfMonth());
		
		
	
		criteriaQuery.where(
				//vamos trabalhar a condicoes para pegar a data quando for maior ou igual ao primeiro dia
				criteriaBuilder.greaterThanOrEqualTo(root.get(Lancamento_.dataVencimento), primeiroDia),
				
				//vamos trabalhar a condicoes para pegar a data quando for menor ou igual ao ultimo dia
				criteriaBuilder.lessThanOrEqualTo(root.get(Lancamento_.dataVencimento), ultimoDia));
		
		//vamos agrupar as informaçoes por categoria
		criteriaQuery.groupBy(root.get(Lancamento_.tipo), root.get(Lancamento_.dataVencimento));
		
		//ira enviar o resultado da solicitacao
		TypedQuery<LancamentoEstatisticaDia> typedQuery = manager.createQuery(criteriaQuery);
		
		return typedQuery.getResultList();
	}
	
	
	//ira gera a informacao pelo mes
	@Override
	public List<LancamentoEstatisticaCategoria> porCategoria(LocalDate mesReferencia) {
		
		CriteriaBuilder criteriaBuilder = manager.getCriteriaBuilder();
		
		//vamos devolver para aplicacao
		CriteriaQuery<LancamentoEstatisticaCategoria> criteriaQuery = criteriaBuilder.
				createQuery(LancamentoEstatisticaCategoria.class);
		
		//onde vamos buscar os dados em Lancamento
		Root<Lancamento> root = criteriaQuery.from(Lancamento.class);
		
		criteriaQuery.select(criteriaBuilder.construct(LancamentoEstatisticaCategoria.class, 
				root.get(Lancamento_.categoria),
				criteriaBuilder.sum(root.get(Lancamento_.valor))));
		
		//ira pegar o primeiro dia do mês
		LocalDate primeiroDia = mesReferencia.withDayOfMonth(1);
		
		//ira pegar o ultimo dia do mês
		LocalDate ultimoDia = mesReferencia.withDayOfMonth(mesReferencia.lengthOfMonth());
		
		
	
		criteriaQuery.where(
				//ira trazer o tipo de Lancamento pertencente a categoria
				criteriaBuilder.equal(root.get(Lancamento_.tipo), TipoLancamento.DESPESA),
				
				//vamos trabalhar a condicoes para pegar a data quando for maior ou igual ao primeiro dia
				criteriaBuilder.greaterThanOrEqualTo(root.get(Lancamento_.dataVencimento), primeiroDia),
				
				//vamos trabalhar a condicoes para pegar a data quando for menor ou igual ao ultimo dia
				criteriaBuilder.lessThanOrEqualTo(root.get(Lancamento_.dataVencimento), ultimoDia));
		
		//vamos agrupar as informaçoes por categoria
		criteriaQuery.groupBy(root.get(Lancamento_.categoria));
		
		//ira enviar o resultado da solicitacao
		TypedQuery<LancamentoEstatisticaCategoria> typedQuery = manager.createQuery(criteriaQuery);
		
		return typedQuery.getResultList();
	}

	
	//metodo filtrar
	@Override
	public Page<Lancamento> filtrar(LancamentoFilter lancamentoFilter, Pageable pageable) {
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Lancamento> criteria = builder.createQuery(Lancamento.class);
		Root<Lancamento> root = criteria.from(Lancamento.class);
		
		
		//cria as restrições		
		Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);
		criteria.where(predicates);
			
		TypedQuery<Lancamento> query = manager.createQuery(criteria);
		adicionarRestricoesDePaginacao(query, pageable);
		
		
		return new PageImpl<>(query.getResultList(), pageable, total(lancamentoFilter));
	}

	//metodo ira realizar a consulta das informacoes
	@Override
	public Page<ResumoLancamento> resumir(LancamentoFilter lancamentoFilter, Pageable pageable) {
	
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<ResumoLancamento> criteria = builder.createQuery(ResumoLancamento.class);
		Root<Lancamento> root = criteria.from(Lancamento.class);
		
		criteria.select(builder.construct(ResumoLancamento.class, 
				root.get(Lancamento_.codigo),root.get(Lancamento_.descricao),
				root.get(Lancamento_.dataVencimento),root.get(Lancamento_.datapagamento),
				root.get(Lancamento_.valor),root.get(Lancamento_.tipo),
				root.get(Lancamento_.categoria).get(Categoria_.nome),
				root.get(Lancamento_.pessoa).get(Pessoa_.nome)));
		
			//cria as restrições		
				Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);
				criteria.where(predicates);
					
				TypedQuery<ResumoLancamento> query = manager.createQuery(criteria);
				adicionarRestricoesDePaginacao(query, pageable);
				
				
				return new PageImpl<>(query.getResultList(), pageable, total(lancamentoFilter));
	}

	//lista de Predicate para retornar a lista
	private Predicate[] criarRestricoes(LancamentoFilter lancamentoFilter, CriteriaBuilder builder,
			Root<Lancamento> root) {
		
		List<Predicate> predicates = new ArrayList<>();
							
		if(!StringUtils.isEmpty(lancamentoFilter.getDescricao())) {
			predicates.add(builder.like(builder.lower(
					root.get(Lancamento_.descricao)), "%" + lancamentoFilter.getDescricao().toLowerCase() + "%"));
		}
		
		if(lancamentoFilter.getDataVencimentoDe() != null) {
			predicates.add(
					builder.greaterThanOrEqualTo(root.get(Lancamento_.dataVencimento),lancamentoFilter.getDataVencimentoDe()));
		}
		
		if(lancamentoFilter.getDataVencimentoAte() != null) {
			predicates.add(
					builder.lessThanOrEqualTo(root.get(Lancamento_.dataVencimento), lancamentoFilter.getDataVencimentoAte()));
		}

		return predicates.toArray(new Predicate[predicates.size()]);
	}

	
	// Metodo para criar paginação das informações 
	private void adicionarRestricoesDePaginacao(TypedQuery<?> query, Pageable pageable) {
		int paginaAtual = pageable.getPageNumber();
		int totalResgistroPorPagina = pageable.getPageSize();
		int primeiroRegistroDaPagina = paginaAtual * totalResgistroPorPagina;
		
		query.setFirstResult(primeiroRegistroDaPagina);
		query.setMaxResults(totalResgistroPorPagina);
	}
	
	private Long total(LancamentoFilter lancamentoFilter) {
		
		CriteriaBuilder builder = manager.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
		Root<Lancamento> root = criteria.from(Lancamento.class);
		
		Predicate [] predicates = criarRestricoes(lancamentoFilter, builder, root);
		criteria.where(predicates);
		
		criteria.select(builder.count(root));
		
		return manager.createQuery(criteria).getSingleResult();
	}


}
