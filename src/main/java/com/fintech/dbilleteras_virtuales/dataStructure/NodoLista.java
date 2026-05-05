package main.java.com.fintech.dbilleteras_virtuales.dataStructure;

public class NodoLista<T> {

    public T valorNodo;

    public NodoLista<T> siguienteNodo;

    public NodoLista(T valorNodo, NodoLista<T> siguienteNodo) {
        this.valorNodo = valorNodo;
        this.siguienteNodo = siguienteNodo;
    }

    public T getValorNodo() {
        return valorNodo;
    }

    public void setValorNodo(T valorNodo) {
        this.valorNodo = valorNodo;
    }

    public NodoLista<T> getSiguienteNodo() {
        return siguienteNodo;
    }

    public void setSiguienteNodo(NodoLista<T> siguienteNodo) {
        this.siguienteNodo = siguienteNodo;
    }

}
