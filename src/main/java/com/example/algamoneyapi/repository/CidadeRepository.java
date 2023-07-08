package com.example.algamoneyapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.algamoneyapi.model.Cidade;

public interface CidadeRepository extends JpaRepository<Cidade, Long> {

	//metodo que ira trazer todas a cidades a partir de um estado
	List<Cidade> findByEstadoCodigo(Long estadoCodigo);
}
