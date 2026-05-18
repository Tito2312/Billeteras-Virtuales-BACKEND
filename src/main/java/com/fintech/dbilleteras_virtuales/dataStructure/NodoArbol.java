package main.java.com.fintech.dbilleteras_virtuales.dataStructure;

import com.fintech.dbilleteras_virtuales.model.User;

public class NodoArbol {

    public User usuario;
    public int puntos;
    public NodoArbol izquierdo;
    public NodoArbol derecho;

    public NodoArbol(User usuario) {
        this.usuario = usuario;
        this.puntos = usuario.getPoints();
        this.izquierdo = null;
        this.derecho = null;
    }
}
