package com.example.algamoneyapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.algamoneyapi.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>{
	
	public Optional<Usuario> findByEmail(String email);
	
	//ira pesquisar o usuario descricao da permissao
	public List<Usuario> findByPermissoesDescricao(String permissaoDescricao); 

}
