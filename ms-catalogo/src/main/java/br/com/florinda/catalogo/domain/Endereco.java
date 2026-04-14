package br.com.florinda.catalogo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Embeddable
public class Endereco {

    @NotBlank
    @Size(max = 200)
    @Column(name = "endereco_logradouro", length = 200)
    private String logradouro;

    @NotBlank
    @Size(max = 10)
    @Column(name = "endereco_numero", length = 10)
    private String numero;

    @NotBlank
    @Size(max = 100)
    @Column(name = "endereco_bairro", length = 100)
    private String bairro;

    @NotBlank
    @Size(max = 100)
    @Column(name = "endereco_cidade", length = 100)
    private String cidade;

    @NotBlank
    @Size(min = 2, max = 2)
    @Column(name = "endereco_uf", columnDefinition = "char(2)")
    private String uf;

    @NotBlank
    @Pattern(regexp = "\\d{5}-\\d{3}", message = "CEP deve ter o formato 00000-000")
    @Column(name = "endereco_cep", length = 9)
    private String cep;

    public Endereco() {}

    public Endereco(String logradouro, String numero, String bairro,
                    String cidade, String uf, String cep) {
        this.logradouro = logradouro;
        this.numero = numero;
        this.bairro = bairro;
        this.cidade = cidade;
        this.uf = uf;
        this.cep = cep;
    }

    public String getLogradouro() { return logradouro; }
    public String getNumero()     { return numero; }
    public String getBairro()     { return bairro; }
    public String getCidade()     { return cidade; }
    public String getUf()         { return uf; }
    public String getCep()        { return cep; }

    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }
    public void setNumero(String numero)         { this.numero = numero; }
    public void setBairro(String bairro)         { this.bairro = bairro; }
    public void setCidade(String cidade)         { this.cidade = cidade; }
    public void setUf(String uf)                 { this.uf = uf; }
    public void setCep(String cep)               { this.cep = cep; }
}
