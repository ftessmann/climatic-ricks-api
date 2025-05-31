package com.climaticrisks.models;

public class RelatorioEvento extends BaseModel {
    private String periodo;
    private String regiao;
    private Integer totalAlagamentos;
    private Integer totalDeslizamentos;
    private Integer totalDiagnosticos;

    public RelatorioEvento() {
        super();
    }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    public String getRegiao() { return regiao; }
    public void setRegiao(String regiao) { this.regiao = regiao; }

    public Integer getTotalAlagamentos() { return totalAlagamentos; }
    public void setTotalAlagamentos(Integer totalAlagamentos) { this.totalAlagamentos = totalAlagamentos; }

    public Integer getTotalDeslizamentos() { return totalDeslizamentos; }
    public void setTotalDeslizamentos(Integer totalDeslizamentos) { this.totalDeslizamentos = totalDeslizamentos; }

    public Integer getTotalDiagnosticos() { return totalDiagnosticos; }
    public void setTotalDiagnosticos(Integer totalDiagnosticos) { this.totalDiagnosticos = totalDiagnosticos; }
}