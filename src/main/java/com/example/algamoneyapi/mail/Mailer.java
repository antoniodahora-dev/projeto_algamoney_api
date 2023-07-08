package com.example.algamoneyapi.mail;

import java.util.HashMap;
//import java.util.Arrays;
//import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.context.event.ApplicationReadyEvent;
//import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.example.algamoneyapi.model.Lancamento;
import com.example.algamoneyapi.model.Usuario;
//import com.example.algamoneyapi.model.Lancamento;
//import com.example.algamoneyapi.repository.LancamentoRepository;

@Component
public class Mailer {

	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private TemplateEngine thymeleaf;
	
	
	/*metodo que ira escutar o envio do email
	@Autowired
	private LancamentoRepository lancRepor;
	@EventListener
	private void teste(ApplicationReadyEvent event) {
		
		this.enviarEmail("antoniodahora.socialmedia@gmail.com", 
				Arrays.asList("dahoraocara@gmail.com"),
				"Testando", "Olá </br> Nova esperança!");
		
		System.out.println("Fim do envio");
	}
	
	@EventListener
	private void teste(ApplicationReadyEvent event) {
		String template = "mail/aviso-lancamentos-vencidos";
		
		List<Lancamento> lista = lancRepor.findAll();
		
		Map<String, Object> variaveis = new HashMap<>();
		variaveis.put("lancamentos", lista);
		
		this.enviarEmail("antoniodahora.socialmedia@gmail.com", 
				Arrays.asList("dahoraocara@gmail.com"),
				"Testando", template, variaveis);
		
		System.out.println("Fim do envio");
	}*/
	
	//metodo que ira avisar quando houve algum Lancamento Vencido
	public void avisarSobreLancamentosVencidos(
			List<Lancamento> vencidos, List<Usuario> destinatarios) {
		
		Map<String, Object> variaveis = new HashMap<>();
		
		//a variavel lancamentos deve ser a mesma informada no html
		variaveis.put("lancamentos", vencidos);
		
		//iremos configurar uma lista de emails
		List<String> emails = destinatarios.stream()
				.map(u -> u.getEmail())
				.collect(Collectors.toList());
		
		this.enviarEmail("dahoraocara@gmail.com", emails, 
				"Lancamentos Vencidos", "mail/aviso-lancamentos-vencidos" ,
				variaveis);
		
	}
	
	
	
	//metodo dispara o envio do email
	public void enviarEmail(String remetente,
				List<String> destinatarios, String assunto, String template, 
				Map<String, Object> variaveis) {
		
		Context context = new Context(new Locale("pt", "BR"));
		
		variaveis.entrySet().forEach(e -> context.setVariable(e.getKey(), e.getValue()));
		
		String mensagem = thymeleaf.process(template, context);
		
		this.enviarEmail(remetente, destinatarios, assunto, mensagem);
	}
	
	
	//metodo dispara o envio do email
	public void enviarEmail(String remetente,
				List<String> destinatarios, String assunto, String mensagem) {
		
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			
			//parametros para o envio do email
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
			helper.setFrom(remetente);
			helper.setTo(destinatarios.toArray(new String[destinatarios.size()]));
			helper.setSubject(assunto);
			helper.setText(mensagem, true);
			
			mailSender.send(mimeMessage);
			
		} catch (MessagingException e) {
			throw new RuntimeException("Problemas com o envio do email", e);
		}
	}
	
}
