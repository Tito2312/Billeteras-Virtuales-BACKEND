package main.java.com.fintech.dbilleteras_virtuales.dataStructure;

public class Cola<T> {
    private NodoCola<T> frente;
    private NodoCola<T> fin;
    private int tamaño;

    public Cola() {
        this.frente = null;
        this.fin = null;
        this.tamaño = 0;
    }

    public void encolar(T dato) {
        NodoCola<T> nuevoNodo = new NodoCola<>(dato);

        if (estaVacia()) {
            frente = nuevoNodo;
            fin = nuevoNodo;
        } else {
            fin.setSiguiente(nuevoNodo);
            fin = nuevoNodo;
        }
        tamaño++;
    }

    public T desencolar() {
        if (estaVacia()) {
            throw new RuntimeException("La cola está vacía");
        }

        T dato = frente.getDato();
        frente = frente.getSiguiente();

        if (frente == null) {
            fin = null;
        }

        tamaño--;
        return dato;
    }

    public T frente() {
        if (estaVacia()) {
            throw new RuntimeException("La cola está vacía");
        }
        return frente.getDato();
    }

    public boolean estaVacia() {
        return frente == null;
    }

    public int getTamaño() {
        return tamaño;
    }

    public void imprimir() {
        if (estaVacia()) {
            System.out.println("La cola está vacía");
            return;
        }

        System.out.println("Cola (frente -> fin):");
        NodoCola<T> actual = frente;
        while (actual != null) {
            System.out.println(actual.getDato());
            actual = actual.getSiguiente();
        }
    }

    public void limpiar() {
        frente = null;
        fin = null;
        tamaño = 0;
    }
}
