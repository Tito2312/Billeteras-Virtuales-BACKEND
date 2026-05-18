package main.java.com.fintech.dbilleteras_virtuales.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fintech.dbilleteras_virtuales.dataStructure.GraphEdge;
import com.fintech.dbilleteras_virtuales.dataStructure.TransferGraph;
import com.fintech.dbilleteras_virtuales.model.Transaction;
import com.fintech.dbilleteras_virtuales.model.TransactionType;
import com.fintech.dbilleteras_virtuales.model.User;
import com.fintech.dbilleteras_virtuales.repository.TransactionRepository;
import com.fintech.dbilleteras_virtuales.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransferGraphService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    private TransferGraph buildGraph() {
        TransferGraph graph = new TransferGraph();

        List<User> users = userRepository.findAll();
        users.forEach(u -> graph.addNode(u.getId(), u.getName()));

        List<Transaction> transfers = transactionRepository
            .findByTypeAndReceiverUserIdNotNull(TransactionType.TRANSFER);

        transfers.forEach(t -> graph.addEdge(
            t.getUserId(),
            t.getReceiverUserId(),
            t.getAmount(),
            t.getId()
        ));

        return graph;
    }

    public List<GraphEdge> getTransfersFrom(String userId) {
        return buildGraph().getTransfersFrom(userId);
    }

    public boolean hasCycle() {
        return buildGraph().hasCycle();
    }

    public List<String> findPath(String sourceUserId, String targetUserId) {
        return buildGraph().findPath(sourceUserId, targetUserId);
    }

    public String getMostActiveUser() {
        return buildGraph().getMostActiveUser();
    }
}