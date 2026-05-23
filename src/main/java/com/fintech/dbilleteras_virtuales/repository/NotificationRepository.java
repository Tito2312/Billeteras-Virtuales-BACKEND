package com.fintech.dbilleteras_virtuales.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.fintech.dbilleteras_virtuales.model.Notification;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByEmail(String email);

    List<Notification> findByUserId(String userId);

}
