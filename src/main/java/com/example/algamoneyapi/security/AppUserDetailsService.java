package com.example.algamoneyapi.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.algamoneyapi.model.Usuario;
import com.example.algamoneyapi.repository.UsuarioRepository;

@Service
public class AppUserDetailsService implements UserDetailsService {

	@Autowired
	private UsuarioRepository usuarioRepository;
	
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Optional<Usuario> usuarioOptional = usuarioRepository.findByEmail(email);
		Usuario usuario = usuarioOptional.orElseThrow(() -> new UsernameNotFoundException ("Usuário ou senha incorreto"));
		//return  new User(email, usuario.getSenha(), getPermissoes(usuario));
		return  new UsuarioSistema(usuario, getPermissoes(usuario));
	}


	//passar as informações para o usuaŕio
	private Collection<? extends GrantedAuthority> getPermissoes(Usuario usuario) {
		
		//são as permissoes
		// uma lista permissoes do usuario
		Set<SimpleGrantedAuthority> authorities = new HashSet<>();
		
		//carrega as informações para o usuário
		usuario.getPermissoes().forEach(p -> authorities.add(new SimpleGrantedAuthority(p.getDescricao().toUpperCase())));
		
		return authorities;
	}




}
