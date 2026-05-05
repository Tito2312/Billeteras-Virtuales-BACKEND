package main.java.com.fintech.dbilleteras_virtuales.dataStructure;

public class ColaPrioridad<T extends Comparable<T>> {
    private NodoColaPrioridad<T> cabeza;
    private int tamaño;
    
    private static class NodoColaPrioridad<T> {
        private T dato;
        private NodoColaPrioridad<T> siguiente;
        
        public NodoColaPrioridad(T dato) {
            this.dato = dato;
            this.siguiente = null;
        }
        
        public T getDato() { return dato; }
        public void setDato(T dato) { this.dato = dato; }
        public NodoColaPrioridad<T> getSiguiente() { return siguiente; }
        public void setSiguiente(NodoColaPrioridad<T> siguiente) { this.siguiente = siguiente; }
    }
    
    public ColaPrioridad() {
        this.cabeza = null;
        this.tamaño = 0;
    }
    
    public void encolar(T dato) {
        NodoColaPrioridad<T> nuevoNodo = new NodoColaPrioridad<>(dato);
        
        if (cabeza == null || dato.compareTo(cabeza.getDato()) < 0) {
            nuevoNodo.setSiguiente(cabeza);
            cabeza = nuevoNodo;
        } else {
            NodoColaPrioridad<T> actual = cabeza;
            while (actual.getSiguiente() != null && 
                   dato.compareTo(actual.getSiguiente().getDato()) >= 0) {
                actual = actual.getSiguiente();
            }
            nuevoNodo.setSiguiente(actual.getSiguiente());
            actual.setSiguiente(nuevoNodo);
        }
        tamaño++;
    }
    
    public T desencolar() {
        if (estaVacia()) {
            throw new RuntimeException("La cola de prioridad está vacía");
        }
        
        T dato = cabeza.getDato();
        cabeza = cabeza.getSiguiente();
        tamaño--;
        return dato;
    }
    
    public T frente() {
        if (estaVacia()) {
            throw new RuntimeException("La cola de prioridad está vacía");
        }
        return cabeza.getDato();
    }
    
    public boolean estaVacia() {
        return cabeza == null;
    }
    
    public int getTamaño() {
        return tamaño;
    }
    
    public void imprimir() {
        if (estaVacia()) {
            System.out.println("La cola de prioridad está vacía");
            return;
        }
        
        System.out.println("Cola de Prioridad (mayor prioridad -> menor prioridad):");
        NodoColaPrioridad<T> actual = cabeza;
        while (actual != null) {
            System.out.println(actual.getDato());
            actual = actual.getSiguiente();
        }
    }
    
    public void limpiar() {
        cabeza = null;
        tamaño = 0;
    }
}
