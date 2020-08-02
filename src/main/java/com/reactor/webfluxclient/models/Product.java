package com.reactor.webfluxclient.models;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Product {

    private String id;
    private String nombre;
    private Double precio;
    private Date createAt;
    private String foto;
    private Category categoria;
}
