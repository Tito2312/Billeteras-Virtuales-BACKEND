package com.fintech.dbilleteras_virtuales.service;

import com.fintech.dbilleteras_virtuales.dataStructure.ListaSimple;
import com.fintech.dbilleteras_virtuales.dataStructure.NodoLista;
import com.fintech.dbilleteras_virtuales.dto.ReportDto;
import com.fintech.dbilleteras_virtuales.model.Transaction;
import com.fintech.dbilleteras_virtuales.model.TransactionType;
import com.fintech.dbilleteras_virtuales.model.Wallet;
import com.fintech.dbilleteras_virtuales.repository.TransactionRepository;
import com.fintech.dbilleteras_virtuales.repository.UserRepository;
import com.fintech.dbilleteras_virtuales.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    private static class ParConteo {
        String clave;
        long conteo;
        double totalMonto;

        ParConteo(String clave) {
            this.clave = clave;
            this.conteo = 0;
            this.totalMonto = 0;
        }
    }

    private ParConteo obtenerOCrear(ListaSimple<ParConteo> lista, String clave) {
        NodoLista<ParConteo> nodo = lista.firtNodo();
        while (nodo != null) {
            if (nodo.getValorNodo().clave.equals(clave)) {
                return nodo.getValorNodo();
            }
            nodo = nodo.getSiguienteNodo();
        }
        ParConteo nuevo = new ParConteo(clave);
        lista.agregar(nuevo);
        return nuevo;
    }

    private void ordenarPorConteoDesc(ListaSimple<ParConteo> lista) {
        if (lista.getTamaño() <= 1) return;
        boolean intercambio;
        do {
            intercambio = false;
            NodoLista<ParConteo> nodo = lista.firtNodo();
            while (nodo != null && nodo.getSiguienteNodo() != null) {
                ParConteo actual = nodo.getValorNodo();
                ParConteo siguiente = nodo.getSiguienteNodo().getValorNodo();
                if (actual.conteo < siguiente.conteo) {
                    nodo.setValorNodo(siguiente);
                    nodo.getSiguienteNodo().setValorNodo(actual);
                    intercambio = true;
                }
                nodo = nodo.getSiguienteNodo();
            }
        } while (intercambio);
    }

    public List<ReportDto.WalletUsageReport> getWalletsWithMostUsage(int topN) {
        List<Transaction> all = transactionRepository.findAll();

        ListaSimple<Transaction> listaTx = new ListaSimple<>();
        for (Transaction t : all) listaTx.agregar(t);

        ListaSimple<ParConteo> conteo = new ListaSimple<>();

        NodoLista<Transaction> nodo = listaTx.firtNodo();
        while (nodo != null) {
            Transaction t = nodo.getValorNodo();
            if (t.getSourceWallet() != null) {
                ParConteo entrada = obtenerOCrear(conteo, t.getSourceWallet());
                entrada.conteo++;
                entrada.totalMonto += t.getAmount();
            }
            if (t.getTargetWallet() != null && !t.getTargetWallet().equals(t.getSourceWallet())) {
                ParConteo entrada = obtenerOCrear(conteo, t.getTargetWallet());
                entrada.conteo++;
            }
            nodo = nodo.getSiguienteNodo();
        }

        ordenarPorConteoDesc(conteo);

        List<ReportDto.WalletUsageReport> resultado = new ArrayList<>();
        NodoLista<ParConteo> nodoC = conteo.firtNodo();
        int i = 0;
        while (nodoC != null && i < topN) {
            ParConteo p = nodoC.getValorNodo();
            Optional<Wallet> wallet = walletRepository.findById(p.clave);
            resultado.add(ReportDto.WalletUsageReport.builder()
                    .walletId(p.clave)
                    .walletName(wallet.map(Wallet::getName).orElse("Desconocida"))
                    .walletType(wallet.map(Wallet::getType).orElse("Desconocido"))
                    .transactionCount(p.conteo)
                    .totalAmount(p.totalMonto)
                    .build());
            nodoC = nodoC.getSiguienteNodo();
            i++;
        }
        return resultado;
    }

    public List<ReportDto.UserTransferReport> getUsersWithMostTransfers(int topN) {
        List<Transaction> transfers = transactionRepository.findByType(TransactionType.TRANSFER);

        ListaSimple<Transaction> listaTx = new ListaSimple<>();
        for (Transaction t : transfers) listaTx.agregar(t);

        ListaSimple<ParConteo> conteo = new ListaSimple<>();

        NodoLista<Transaction> nodo = listaTx.firtNodo();
        while (nodo != null) {
            Transaction t = nodo.getValorNodo();
            ParConteo entrada = obtenerOCrear(conteo, t.getUserId());
            entrada.conteo++;
            entrada.totalMonto += t.getAmount();
            nodo = nodo.getSiguienteNodo();
        }

        ordenarPorConteoDesc(conteo);

        List<ReportDto.UserTransferReport> resultado = new ArrayList<>();
        NodoLista<ParConteo> nodoC = conteo.firtNodo();
        int i = 0;
        while (nodoC != null && i < topN) {
            ParConteo p = nodoC.getValorNodo();
            userRepository.findById(p.clave).ifPresent(user ->
                    resultado.add(ReportDto.UserTransferReport.builder()
                            .userId(user.getId())
                            .userName(user.getName())
                            .userEmail(user.getEmail())
                            .transferCount(p.conteo)
                            .totalTransferred(p.totalMonto)
                            .build()));
            nodoC = nodoC.getSiguienteNodo();
            i++;
        }
        return resultado;
    }

    public List<ReportDto.CategoryActivityReport> getMostActiveWalletCategories() {
        List<Wallet> allWallets = walletRepository.findAll();
        List<Transaction> allTransactions = transactionRepository.findAll();

        ListaSimple<Wallet> listaWallets = new ListaSimple<>();
        for (Wallet w : allWallets) listaWallets.agregar(w);

        ListaSimple<Transaction> listaTx = new ListaSimple<>();
        for (Transaction t : allTransactions) listaTx.agregar(t);

        ListaSimple<ParConteo> conteoCategoria = new ListaSimple<>();

        NodoLista<Transaction> nodoT = listaTx.firtNodo();
        while (nodoT != null) {
            Transaction t = nodoT.getValorNodo();
            String tipo = resolverTipoWallet(listaWallets, t.getSourceWallet());
            ParConteo entrada = obtenerOCrear(conteoCategoria, tipo);
            entrada.conteo++;
            entrada.totalMonto += t.getAmount();
            nodoT = nodoT.getSiguienteNodo();
        }

        ListaSimple<ParConteo> conteoBilleteras = new ListaSimple<>();
        NodoLista<Wallet> nodoWC = listaWallets.firtNodo();
        while (nodoWC != null) {
            Wallet w = nodoWC.getValorNodo();
            ParConteo entrada = obtenerOCrear(conteoBilleteras, w.getType());
            entrada.conteo++;
            nodoWC = nodoWC.getSiguienteNodo();
        }

        ordenarPorConteoDesc(conteoCategoria);

        List<ReportDto.CategoryActivityReport> resultado = new ArrayList<>();
        NodoLista<ParConteo> nodoC = conteoCategoria.firtNodo();
        while (nodoC != null) {
            ParConteo p = nodoC.getValorNodo();
            long cantBilleteras = buscarConteo(conteoBilleteras, p.clave);
            resultado.add(ReportDto.CategoryActivityReport.builder()
                    .category(p.clave)
                    .transactionCount(p.conteo)
                    .walletCount(cantBilleteras)
                    .totalAmount(p.totalMonto)
                    .build());
            nodoC = nodoC.getSiguienteNodo();
        }
        return resultado;
    }

    public List<ReportDto.TransactionFrequencyReport> getTransactionFrequencyByType() {
        List<Transaction> all = transactionRepository.findAll();

        ListaSimple<Transaction> listaTx = new ListaSimple<>();
        for (Transaction t : all) listaTx.agregar(t);

        ListaSimple<ParConteo> conteo = new ListaSimple<>();

        NodoLista<Transaction> nodo = listaTx.firtNodo();
        while (nodo != null) {
            Transaction t = nodo.getValorNodo();
            ParConteo entrada = obtenerOCrear(conteo, t.getType().name());
            entrada.conteo++;
            entrada.totalMonto += t.getAmount();
            nodo = nodo.getSiguienteNodo();
        }

        ordenarPorConteoDesc(conteo);

        List<ReportDto.TransactionFrequencyReport> resultado = new ArrayList<>();
        NodoLista<ParConteo> nodoC = conteo.firtNodo();
        while (nodoC != null) {
            ParConteo p = nodoC.getValorNodo();
            resultado.add(ReportDto.TransactionFrequencyReport.builder()
                    .type(p.clave)
                    .count(p.conteo)
                    .totalAmount(p.totalMonto)
                    .build());
            nodoC = nodoC.getSiguienteNodo();
        }
        return resultado;
    }

    public ReportDto.DateRangeSummaryReport getTotalAmountInDateRange(LocalDateTime start, LocalDateTime end) {
        List<Transaction> transactions = transactionRepository.findByCreatedAtBetween(start, end);

        ListaSimple<Transaction> listaTx = new ListaSimple<>();
        for (Transaction t : transactions) listaTx.agregar(t);

        double totalMonto = 0;
        ListaSimple<ParConteo> conteoTipo = new ListaSimple<>();

        NodoLista<Transaction> nodo = listaTx.firtNodo();
        while (nodo != null) {
            Transaction t = nodo.getValorNodo();
            totalMonto += t.getAmount();
            ParConteo entrada = obtenerOCrear(conteoTipo, t.getType().name());
            entrada.totalMonto += t.getAmount();
            nodo = nodo.getSiguienteNodo();
        }

        Map<String, Double> montoPorTipo = new HashMap<>();
        NodoLista<ParConteo> nodoC = conteoTipo.firtNodo();
        while (nodoC != null) {
            montoPorTipo.put(nodoC.getValorNodo().clave, nodoC.getValorNodo().totalMonto);
            nodoC = nodoC.getSiguienteNodo();
        }

        return ReportDto.DateRangeSummaryReport.builder()
                .startDate(start)
                .endDate(end)
                .transactionCount(listaTx.getTamaño())
                .totalAmount(totalMonto)
                .amountByType(montoPorTipo)
                .build();
    }

    private String resolverTipoWallet(ListaSimple<Wallet> listaWallets, String walletId) {
        if (walletId == null) return "Desconocido";
        NodoLista<Wallet> nodo = listaWallets.firtNodo();
        while (nodo != null) {
            if (nodo.getValorNodo().getId().equals(walletId)) {
                return nodo.getValorNodo().getType();
            }
            nodo = nodo.getSiguienteNodo();
        }
        return "Desconocido";
    }

    private long buscarConteo(ListaSimple<ParConteo> lista, String clave) {
        NodoLista<ParConteo> nodo = lista.firtNodo();
        while (nodo != null) {
            if (nodo.getValorNodo().clave.equals(clave)) {
                return nodo.getValorNodo().conteo;
            }
            nodo = nodo.getSiguienteNodo();
        }
        return 0;
    }
}
