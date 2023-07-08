package com.example.algamoneyapi.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.algamoneyapi.model.Lancamento;
import com.example.algamoneyapi.repository.lancamento.LancamentoRepositoryQuery;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long>, LancamentoRepositoryQuery{

	//ira pesquisar os pagamentos que encontra-se em aberto pela DataVencimento
	//DataVencimento deve ser menor ou igual aos parametros e DataPagamento deve ser null
	List<Lancamento> findByDataVencimentoLessThanEqualAndDatapagamentoIsNull(LocalDate data);
}