package com.example.algamoneyapi.storage;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.ObjectTagging;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.SetObjectTaggingRequest;
import com.amazonaws.services.s3.model.Tag;
import com.example.algamoneyapi.config.property.AlgamoneyApiProperty;

@Component
public class S3 {
	
	private static final Logger logger = LoggerFactory.getLogger(S3.class);
	
	@Autowired
	private AlgamoneyApiProperty property;
	
	@Autowired
	private AmazonS3 amazonS3;
	
	public String salvarTemporariamente(MultipartFile arquivo) {
		
		AccessControlList acl = new AccessControlList();
		
		//objeto podera ser lido
		acl.grantPermission(GroupGrantee.AllUsers, Permission.Read);
		
		
		ObjectMetadata objectMetadata = new ObjectMetadata();
		
		objectMetadata.setContentType(arquivo.getContentType());
		objectMetadata.setContentLength(arquivo.getSize());
		
		//ira gerar um nome unico
		String nomeUnico = gerarNomeUnico(arquivo.getOriginalFilename());
		
		try {
			PutObjectRequest putObjectRequest = new PutObjectRequest(
					property.getS3().getBucket(),
					nomeUnico,
					arquivo.getInputStream(),
					objectMetadata)
					.withAccessControlList(acl);
			
			//tempo de expiracao
			putObjectRequest.setTagging(new ObjectTagging(
					Arrays.asList(new Tag("expirar" , "true"))));
		
			//ira enviar o arquivo para o S3
			amazonS3.putObject(putObjectRequest);
			
			//ira realizar o debug no metodo
			if (logger.isDebugEnabled()) {
				logger.debug("Arquivo enviado com sucesso para o S3." , 
						arquivo.getOriginalFilename());
			}
			
			return nomeUnico;
		
		//ira tratar caso tenha um algum erro no envio	
		} catch (IOException e) {
			throw new RuntimeException("Problemas ao tentar enviar o arquivo para o S3.", e);
		}
		
		
	}
	
	//ira configurar a url de acesso ao arquivo anexado
	public String configurarUrl(String objeto) {
		return "\\\\" + property.getS3().getBucket() +
				".s3.amazonaws.com/" + objeto;
	}

	//metodo que ira salvar o anexo e sobrescrever as tags temporarias
	public void salvar(String objeto) {
		SetObjectTaggingRequest setObjectTaggingRequest = new SetObjectTaggingRequest( 
				property.getS3().getBucket(),
				objeto,
				new ObjectTagging(Collections.emptyList())); //ira sobrescrever a tags temporaria por um tag vazia
		
		//passaremos a requisão que foi criada
		amazonS3.setObjectTagging(setObjectTaggingRequest);
		
	}

	//metodo para remover o arquivo (anexo) no amazonS3
	public void remover(String objeto) {
		DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(
				property.getS3().getBucket(), objeto);
		
		amazonS3.deleteObject(deleteObjectRequest);
	}

	//ira substituir e/ou remover o anexo antigo
	public void substituir(String objetoAntigo, String objetoNovo) {
		
		if (StringUtils.hasText(objetoAntigo)) {
			this.remover(objetoAntigo);
		}
		
		salvar(objetoNovo);
		
	}

	//ira gerar o nome unico para o arquivo que sera amarzenado no S3
	private String gerarNomeUnico(String originalFilename) {

		return UUID.randomUUID().toString() + "_" + originalFilename;
	}



}
