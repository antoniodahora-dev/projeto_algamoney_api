package com.example.algamoneyapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.Tag;
import com.amazonaws.services.s3.model.lifecycle.LifecycleFilter;
import com.amazonaws.services.s3.model.lifecycle.LifecycleTagPredicate;
import com.example.algamoneyapi.config.property.AlgamoneyApiProperty;

@Configuration
public class S3Config {

	@Autowired
	private AlgamoneyApiProperty property;
	
	@Bean
	public AmazonS3 amazonS3() {
		AWSCredentials credenciais = new BasicAWSCredentials(
				property.getS3().getAccessKeyId(), property.getS3().getSecretAccessKey());
		
		AmazonS3 amazonS3 = AmazonS3ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credenciais))
				.withRegion(Regions.US_EAST_2)
				.build();
		
		//ira verificar se o bucket existe 
		//caso não exista ira criar automaticamente
		if (!amazonS3.doesBucketExistV2(property.getS3().getBucket())) {
			
			amazonS3.createBucket(
					new CreateBucketRequest(property.getS3().getBucket()));
			
			//quando bucket expirar 
			//metodo ira limpar as informacoes do arquivos temporarios
			BucketLifecycleConfiguration.Rule regraExpiracao = 
					new BucketLifecycleConfiguration.Rule()
					//todos os arquivos que tiverem com a tag "expirar" serao eliminados
					.withId("Regra de Expiração de arquivos temporários")
					.withFilter(new LifecycleFilter(
							new LifecycleTagPredicate(new Tag("expirar" , "true"))))
					
					//esta informando que ira expirar em um dia
					.withExpirationInDays(1)
					
					// a regra esta autorizada
					.withStatus(BucketLifecycleConfiguration.ENABLED);
			
			//objeto de configuração
			BucketLifecycleConfiguration configuration = new BucketLifecycleConfiguration()
					.withRules(regraExpiracao); 
			
			//associando a regra ao Bucket criado
			amazonS3.setBucketLifecycleConfiguration(property.getS3().getBucket(), configuration);
		}
	
	return amazonS3;
	
	}
}
