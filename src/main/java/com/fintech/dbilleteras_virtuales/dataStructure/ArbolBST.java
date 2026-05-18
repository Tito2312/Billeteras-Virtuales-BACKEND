package main.java.com.fintech.dbilleteras_virtuales.dataStructure;

import com.fintech.dbilleteras_virtuales.model.User;
import java.util.ArrayList;
import java.util.List;

public class ArbolBST {

    private NodoArbol raiz;

    public ArbolBST() {
        this.raiz = null;
    }

    public void insertar(User usuario) {
        raiz = insertarRecursivo(raiz, usuario);
    }

    private NodoArbol insertarRecursivo(NodoArbol nodo, User usuario) {
        if (nodo == null) {
            return new NodoArbol(usuario);
        }
        if (usuario.getPoints() < nodo.puntos) {
            nodo.izquierdo = insertarRecursivo(nodo.izquierdo, usuario);
        } else if (usuario.getPoints() > nodo.puntos) {
            nodo.derecho = insertarRecursivo(nodo.derecho, usuario);
        } else {
            nodo.derecho = insertarRecursivo(nodo.derecho, usuario);
        }
        return nodo;
    }
    public List<User> inorden() {
        List<User> resultado = new ArrayList<>();
        inordenRecursivo(raiz, resultado);
        return resultado;
    }

    private void inordenRecursivo(NodoArbol nodo, List<User> resultado) {
        if (nodo != null) {
            inordenRecursivo(nodo.izquierdo, resultado);
            resultado.add(nodo.usuario);
            inordenRecursivo(nodo.derecho, resultado);
        }
    }

    public List<User> buscarPorRango(int minPuntos, int maxPuntos) {
        List<User> resultado = new ArrayList<>();
        buscarRangoRecursivo(raiz, minPuntos, maxPuntos, resultado);
        return resultado;
    }

    private void buscarRangoRecursivo(NodoArbol nodo, int min, int max, List<User> resultado) {
        if (nodo == null) return;

        if (nodo.puntos > min) {
            buscarRangoRecursivo(nodo.izquierdo, min, max, resultado);
        }
        if (nodo.puntos >= min && nodo.puntos <= max) {
            resultado.add(nodo.usuario);
        }
        if (nodo.puntos < max) {
            buscarRangoRecursivo(nodo.derecho, min, max, resultado);
        }
    }

    public User obtenerMaximo() {
        if (raiz == null) return null;
        NodoArbol actual = raiz;
        while (actual.derecho != null) {
            actual = actual.derecho;
        }
        return actual.usuario;
    }
    public List<User> buscarPorNivel(String nivel) {
        int min, max;
        switch (nivel.toUpperCase()) {
            case "BRONZE": min = 0;    max = 500;        break;
            case "SILVER": min = 501;  max = 1000;       break;
            case "GOLD":   min = 1001; max = 5000;       break;
            case "PLATINUM": min = 5001; max = Integer.MAX_VALUE; break;
            default: return new ArrayList<>();
        }
        return buscarPorRango(min, max);
    }

    public boolean estaVacio() {
        return raiz == null;
    }
}