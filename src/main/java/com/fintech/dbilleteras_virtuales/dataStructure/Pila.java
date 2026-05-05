package main.java.com.fintech.dbilleteras_virtuales.dataStructure;

public class Pila<T> {
    private NodoPila<T> cima;
    private int tamaño;
    
    public Pila() {
        this.cima = null;
        this.tamaño = 0;
    }
    
    public void apilar(T dato) {
        NodoPila<T> nuevoNodo = new NodoPila<>(dato);
        nuevoNodo.setSiguiente(cima);
        cima = nuevoNodo;
        tamaño++;
    }
    
    public T desapilar() {
        if (estaVacia()) {
            throw new RuntimeException("La pila está vacía");
        }
        
        T dato = cima.getDato();
        cima = cima.getSiguiente();
        tamaño--;
        return dato;
    }
    
    public T cima() {
        if (estaVacia()) {
            throw new RuntimeException("La pila está vacía");
        }
        return cima.getDato();
    }
    
    public boolean estaVacia() {
        return cima == null;
    }
    
    public int getTamaño() {
        return tamaño;
    }
    
    public void imprimir() {
        if (estaVacia()) {
            System.out.println("La pila está vacía");
            return;
        }
        
        System.out.println("Pila (cima -> base):");
        NodoPila<T> actual = cima;
        while (actual != null) {
            System.out.println(actual.getDato());
            actual = actual.getSiguiente();
        }
    }
    
    public void limpiar() {
        cima = null;
        tamaño = 0;
    }
}
