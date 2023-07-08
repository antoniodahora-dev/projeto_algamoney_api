package com.example.algamoneyapi.repository.listener;

import javax.persistence.PostLoad;

import org.springframework.util.StringUtils;

import com.example.algamoneyapi.AlgamoneyApiApplication;
import com.example.algamoneyapi.model.Lancamento;
import com.example.algamoneyapi.storage.S3;

public class LancamentoAnexoListener {

	@PostLoad
	public void postLoad(Lancamento lancamento) {
		
		//ira preencher a Url se tiver um anexo
		if (StringUtils.hasText(lancamento.getAnexo())) {
			S3 s3 = AlgamoneyApiApplication.getBean(S3.class);
			lancamento.setUrlAnexo(s3.configurarUrl(lancamento.getAnexo()));
		}
	}
	
}
