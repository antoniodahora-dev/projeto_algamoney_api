package com.example.algamoneyapi.config.property;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/*
 *Classe que ira alternar de modo externo os "Todo" de false/true 
 */

@ConfigurationProperties("algamoney")
public class AlgamoneyApiProperty {
	
	private String originPermitida = "http://localhost:4200"; //"https://projeto-algamoney-ui.herokuapp.com";
	
	//pertmir acesso a porta de forma externa
	//private String originPermitida = getOriginPermitida();
	
	private final Seguranca seguranca = new Seguranca ();
	
	private  final Mail mail = new Mail();
	
	private S3 s3 = new S3();
	
	public S3 getS3() {
		return s3;
	}
	
	public Mail getMail() {
		return mail;
	}
	
	
	public Seguranca getSeguranca() {
		return seguranca;
	}

	
	
	public String getOriginPermitida() {
		return originPermitida;
	}

	public void setOriginPermitida(String originPermitida) {
		this.originPermitida = originPermitida;
	}


	//nos dara acesso ao armazenamento no servico da amazon S3
	public static class S3 {
		
		private String accessKeyId;
		private String secretAccessKey;
		
		//o bucket deve ser único
		private String bucket = "ad-soft-arquivos";
		
		public String getBucket() {
			return bucket;
		}
		
		public void setBucket(String bucket) {
			this.bucket = bucket;
		}
		
		public String getAccessKeyId() {
			return accessKeyId;
		}
		public void setAccessKeyId(String accessKeyId) {
			this.accessKeyId = accessKeyId;
		}
		public String getSecretAccessKey() {
			return secretAccessKey;
		}
		public void setSecretAccessKey(String secretAccessKey) {
			this.secretAccessKey = secretAccessKey;
		}
	
	}
	
	

	public static class Seguranca {
		
		/* -- Não serão mais usada --
		private boolean enableHttps;

		public boolean isEnableHttps() {
			return enableHttps;
		}

		public void setEnableHttps(boolean enableHttps) {
			this.enableHttps = enableHttps;
		}*/
		
		//será uma lista de strings
		private List<String> redirectsPermitidos;
		private String authServerUrl;
		

		public List<String> getRedirectsPermitidos() {
			return redirectsPermitidos;
		}

		public void setRedirectsPermitidos(List<String> redirectsPermitidos) {
			this.redirectsPermitidos = redirectsPermitidos;
		}

		
		public String getAuthServerUrl() {
			return authServerUrl;
		}

		public void setAuthServerUrl(String authServerUrl) {
			this.authServerUrl = authServerUrl;
		}

		
			
		
		
	}
	
	//metodo que ira habilitar as configuracoes para o envio de email automatico
	public static class Mail {
	
		private String host;
		private Integer port;
		private String username;
		private String password;
		
		public String getHost() {
			return host;
		}
		
		public void setHost(String host) {
			this.host = host;
		}
		
		public Integer getPort() {
			return port;
		}
		
		public void setPort(Integer port) {
			this.port = port;
		}
		
		public String getUsername() {
			return username;
		}
		
		public void setUsername(String username) {
			this.username = username;
		}
		
		public String getPassword() {
			return password;
		}
		
		public void setPassword(String password) {
			this.password = password;
		}
		
		
	}

}
