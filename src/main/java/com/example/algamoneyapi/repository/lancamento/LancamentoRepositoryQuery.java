package com.example.algamoneyapi.repository.lancamento;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.algamoneyapi.dto.LancamentoEstatisticaCategoria;
import com.example.algamoneyapi.dto.LancamentoEstatisticaDia;
import com.example.algamoneyapi.dto.LancamentoEstatisticasPessoa;
import com.example.algamoneyapi.model.Lancamento;
import com.example.algamoneyapi.repository.filter.LancamentoFilter;
import com.example.algamoneyapi.repository.projection.ResumoLancamento;

public interface LancamentoRepositoryQuery {
	
	//iremos pegar as informacoes por pessoa
	public List<LancamentoEstatisticasPessoa> porPessoa(LocalDate inicio, LocalDate fim);
	
	//iremos pegar as informacoes pelo mes 
	public List<LancamentoEstatisticaCategoria> porCategoria(LocalDate mesReferencia);
	
	//iremos pegar as informacoes pelo dia 
	public List<LancamentoEstatisticaDia> porDia(LocalDate mesReferencia);
	
	public Page<Lancamento> filtrar(LancamentoFilter lancamentoFilter, Pageable pageable);
	
	//metodo que ira realizar o resumo das informacoes na classe Lancament
	public Page<ResumoLancamento> resumir(LancamentoFilter lancamentoFilter, Pageable pageable);
}
