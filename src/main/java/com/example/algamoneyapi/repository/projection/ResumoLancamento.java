package com.example.algamoneyapi.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;		
import com.example.algamoneyapi.model.TipoLancamento;

/*
 * Essa classe ira criar um resumo/projecao das informacoes.
 * Ira retornar apenas o que a gente precisa da classe Lancamento. 
 */
public class ResumoLancamento {

	private Long codigo;		
	private String descricao;	
	private LocalDate dataVencimento;	
	private LocalDate datapagamento;
	private BigDecimal valor;	
	private TipoLancamento tipo;
	private String categoria;	
	private String pessoa;
	
	// iremos utilizar para que seja realizada a consulta	
	public ResumoLancamento(Long codigo, String descricao, LocalDate dataVencimento, LocalDate datapagamento,
			BigDecimal valor, TipoLancamento tipo, String categoria, String pessoa) {
		super();
		this.codigo = codigo;
		this.descricao = descricao;
		this.dataVencimento = dataVencimento;
		this.datapagamento = datapagamento;
		this.valor = valor;
		this.tipo = tipo;
		this.categoria = categoria;
		this.pessoa = pessoa;
	}
	
	
	public Long getCodigo() {
		return codigo;
	}
	public void setCodigo(Long codigo) {
		this.codigo = codigo;
	}
	public String getDescricao() {
		return descricao;
	}
	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
	public LocalDate getDataVencimento() {
		return dataVencimento;
	}
	public void setDataVencimento(LocalDate dataVencimento) {
		this.dataVencimento = dataVencimento;
	}
	public LocalDate getDatapagamento() {
		return datapagamento;
	}
	public void setDatapagamento(LocalDate datapagamento) {
		this.datapagamento = datapagamento;
	}
	public BigDecimal getValor() {
		return valor;
	}
	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}

	public TipoLancamento getTipo() {
		return tipo;
	}
	public void setTipo(TipoLancamento tipo) {
		this.tipo = tipo;
	}
	public String getCategoria() {
		return categoria;
	}
	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}
	public String getPessoa() {
		return pessoa;
	}
	public void setPessoa(String pessoa) {
		this.pessoa = pessoa;
	}
	
	

	

}
