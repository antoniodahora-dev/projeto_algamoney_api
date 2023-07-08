package com.example.algamoneyapi.config;

import java.io.File;
import java.security.KeyStore;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwsEncoder;
import org.springframework.security.oauth2.server.authorization.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.ClientSettings;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.security.oauth2.server.authorization.config.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;

import com.example.algamoneyapi.config.property.AlgamoneyApiProperty;
import com.example.algamoneyapi.security.UsuarioSistema;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

//iremos utilizar apenas essa classe para validar a segurança do OAUTH

@Configuration
@Profile("oauth-security")
public class AuthServerConfig {
	
	@Autowired
	private PasswordEncoder passwordEncoder;
   

	
	@Autowired
	private AlgamoneyApiProperty algamoneyApiProperty;
	
	
	//configuracoes do clients do sistema
	  @Bean
	  public RegisteredClientRepository registeredClientRepository() {
	        
		  RegisteredClient angularClient = RegisteredClient //cliente angular
		            .withId(UUID.randomUUID().toString())
	                .clientId("angular")
	                .clientSecret(passwordEncoder.encode("@admin"))
	                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
	                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
	                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
	                .redirectUris(uris -> uris.addAll(algamoneyApiProperty.getSeguranca().getRedirectsPermitidos()))
	                .scope("read")
	                .scope("write")
	                .tokenSettings(TokenSettings.builder()
	                        .accessTokenTimeToLive(Duration.ofMinutes(30))  //tempo minutos
	                        .refreshTokenTimeToLive(Duration.ofDays(24))  //tempo 24 dias
	                        .build())
	                .clientSettings(ClientSettings.builder()
	                                .requireAuthorizationConsent(true)
	                                .build())
	                .build();

	        RegisteredClient mobileClient = RegisteredClient
	                .withId(UUID.randomUUID().toString())
	                .clientId("mobile")
	                .clientSecret(passwordEncoder.encode("m0b1le"))
	                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
	                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
	                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
	                .redirectUris(uris -> uris.addAll(algamoneyApiProperty.getSeguranca().getRedirectsPermitidos()))
	                .scope("read")
	                .tokenSettings(TokenSettings.builder()
	                        .accessTokenTimeToLive(Duration.ofMinutes(30))
	                        .refreshTokenTimeToLive(Duration.ofDays(24))
	                        .build())
	                .clientSettings(ClientSettings.builder()
	                        .requireAuthorizationConsent(false)
	                        .build())
	                .build();


	        return new InMemoryRegisteredClientRepository(
	                Arrays.asList(
	                        angularClient,
	                        mobileClient
	                )
	        );
	}
	
	
	
	@Bean 
	@Order(Ordered.HIGHEST_PRECEDENCE) // filtro de seguranca - metodo ira verificar o Login e nao mais o Front-End
	public SecurityFilterChain authServerFilterChain(HttpSecurity http) throws Exception {
		
		OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
		return http.formLogin(Customizer.withDefaults()).build();
	}
	
	
	//configuracoes do  JWT
	@Bean
	public OAuth2TokenCustomizer<JwtEncodingContext> jwtBuilderCustomizer() {
			
		return (context) -> {
			UsernamePasswordAuthenticationToken authenticationToken = context.getPrincipal();
			UsuarioSistema usuarioSistema = (UsuarioSistema) authenticationToken.getPrincipal();
			
			Set<String> authorities = new HashSet<>();
			for (GrantedAuthority grandAuthority : usuarioSistema.getAuthorities()) {
				authorities.add(grandAuthority.getAuthority());
			}
			
			context.getClaims().claim("nome", usuarioSistema.getUsuario().getNome());
			context.getClaims().claim("authorities", authorities);
		};
		
	}
	
	
	@Bean //configuração da assinatura do token
	public JWKSet jwkSet() throws Exception {
		
		/*desta forma geramos a chaves publica e privada dentro do projeto*/
		/*RSAKey rsa = new RSAKeyGenerator(2048) // 2048 é o tamanho da chave
				.keyUse(KeyUse.SIGNATURE)
				.keyID(UUID.randomUUID().toString())
				.generate();
		
		return new JWKSet(rsa); // chave publica e privada*/
		/************************************************************/
		
		
		//vamos gerar as chaves publica e privada fora do projeto
		File  file = new ClassPathResource("keystore/algamoney.jks").getFile();
		
		KeyStore keyStore = KeyStore.Builder.newInstance(file,
				new KeyStore.PasswordProtection("123456".toCharArray())
				).getKeyStore();
		
		RSAKey rsaKey = RSAKey.load(
				keyStore, 
				"algamoney",
				"123456".toCharArray()
				);
		
		return new JWKSet(rsaKey);
	}


	
	@Bean //ira ler o JWKSet
	public JWKSource<SecurityContext> jwkSource(JWKSet jwkSet) {
		return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
	}
	
	
	@Bean 
	public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
		return new NimbusJwsEncoder(jwkSource);
	}
	
	@Bean //quem criou o token
	public ProviderSettings providerSettings() {
		return ProviderSettings.builder()
				.issuer(algamoneyApiProperty.getSeguranca().getAuthServerUrl())
				.build();
	}

}
