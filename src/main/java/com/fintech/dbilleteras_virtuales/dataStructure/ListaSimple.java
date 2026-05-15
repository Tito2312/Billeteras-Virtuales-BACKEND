package com.fintech.dbilleteras_virtuales.dataStructure;

public class ListaSimple<T> {

    public NodoLista<T> primerNodoLista;
    public NodoLista<T> ultimNodoLista;
    public int tamaño;

    public ListaSimple() {
        this.primerNodoLista = null;
        this.ultimNodoLista = null;
        this.tamaño = 0;
    }

    public void agregar(T valor) {
        NodoLista<T> nuevoNodo = new NodoLista<>(valor, null);

        if (primerNodoLista == null) {
            primerNodoLista = nuevoNodo;
            ultimNodoLista = nuevoNodo;
        } else {
            ultimNodoLista.setSiguienteNodo(nuevoNodo);
            ultimNodoLista = nuevoNodo;
        }
        tamaño++;
    }

    public void eliminar(T valor) {
        if (primerNodoLista == null) {
            return;
        }

        if (primerNodoLista.getValorNodo().equals(valor)) {
            primerNodoLista = primerNodoLista.getSiguienteNodo();
            if (primerNodoLista == null) {
                ultimNodoLista = null;
            }
            tamaño--;
            return;
        }

        NodoLista<T> actual = primerNodoLista;
        while (actual.getSiguienteNodo() != null) {
            if (actual.getSiguienteNodo().getValorNodo().equals(valor)) {
                actual.setSiguienteNodo(actual.getSiguienteNodo().getSiguienteNodo());
                if (actual.getSiguienteNodo() == null) {
                    ultimNodoLista = actual;
                }
                tamaño--;
                return;
            }
            actual = actual.getSiguienteNodo();
        }
    }

    public T buscarPorIndice(int indice) {
        if (indice < 0 || indice >= tamaño) {
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + indice);
        }

        NodoLista<T> actual = primerNodoLista;
        for (int i = 0; i < indice; i++) {
            actual = actual.getSiguienteNodo();
        }
        return actual.getValorNodo();
    }

    public void imprimir() {
        if (primerNodoLista == null) {
            System.out.println("La lista está vacía");
            return;
        }

        NodoLista<T> actual = primerNodoLista;
        System.out.print("Lista: ");
        while (actual != null) {
            System.out.print(actual.getValorNodo());
            if (actual.getSiguienteNodo() != null) {
                System.out.print(" -> ");
            }
            actual = actual.getSiguienteNodo();
        }
        System.out.println();
    }

    public boolean estaVacia() {
        return primerNodoLista == null;
    }

    public int getTamaño() {
        return tamaño;
    }

    public T buscarPorValor(T valor) {
        NodoLista<T> actual = primerNodoLista;

        while (actual != null) {
            if (actual.getValorNodo().equals(valor)) {
                return actual.getValorNodo();
            }
            actual = actual.getSiguienteNodo();
        }

        return null;
    }

    public T buscarPorId(String id) {
        NodoLista<T> actual = primerNodoLista;

        while (actual != null) {

            if (actual.getValorNodo().toString().contains(id)) {
                return actual.getValorNodo();
            }
            actual = actual.getSiguienteNodo();
        }

        return null;
    }

    public NodoLista<T> firtNodo() {
        return primerNodoLista;
    }

}
