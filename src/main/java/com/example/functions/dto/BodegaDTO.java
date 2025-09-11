package com.example.functions.dto;

public class BodegaDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String direccion;
    private String telefono;
    private Integer capacidadMaxima;
    private Integer espacioUtilizado;
    private Boolean activo;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public Integer getCapacidadMaxima() { return capacidadMaxima; }
    public void setCapacidadMaxima(Integer capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }
    public Integer getEspacioUtilizado() { return espacioUtilizado; }
    public void setEspacioUtilizado(Integer espacioUtilizado) { this.espacioUtilizado = espacioUtilizado; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}