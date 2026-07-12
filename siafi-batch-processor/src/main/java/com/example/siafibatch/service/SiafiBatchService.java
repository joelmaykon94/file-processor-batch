package com.example.siafibatch.service;

import com.example.siafibatch.controller.dto.SiafiNotification;
import com.example.siafibatch.model.DhCarga;
import com.example.siafibatch.model.PfCarga;
import com.example.siafibatch.repository.DhCargaRepository;
import com.example.siafibatch.repository.PfCargaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class SiafiBatchService {

    private final DhCargaRepository dhCargaRepository;
    private final PfCargaRepository pfCargaRepository;
    
    // In-memory list for dynamic notifications
    private final List<SiafiNotification> notifications = new CopyOnWriteArrayList<>();

    public SiafiBatchService(DhCargaRepository dhCargaRepository, PfCargaRepository pfCargaRepository) {
        this.dhCargaRepository = dhCargaRepository;
        this.pfCargaRepository = pfCargaRepository;
        
        // Initial welcome notification
        addNotification("SIAFI Watcher initialized and active.", "INFO");
    }

    public void addNotification(String message, String type) {
        SiafiNotification notification = new SiafiNotification(
            UUID.randomUUID().toString(),
            message,
            type,
            LocalDateTime.now(),
            false
        );
        notifications.add(notification);
        
        // Keep only the last 50 notifications
        if (notifications.size() > 50) {
            notifications.remove(0);
        }
    }

    public List<SiafiNotification> getNotifications() {
        return new ArrayList<>(notifications);
    }

    public List<SiafiNotification> getUnreadNotifications() {
        return notifications.stream()
                .filter(n -> !n.read())
                .collect(Collectors.toList());
    }

    public void markAllAsRead() {
        for (int i = 0; i < notifications.size(); i++) {
            SiafiNotification old = notifications.get(i);
            if (!old.read()) {
                notifications.set(i, new SiafiNotification(
                    old.id(),
                    old.message(),
                    old.type(),
                    old.timestamp(),
                    true
                ));
            }
        }
    }

    public long getDhCount() {
        return dhCargaRepository.count();
    }

    public long getPfCount() {
        return pfCargaRepository.count();
    }

    public List<DhCarga> getDhBatches() {
        return dhCargaRepository.findAll();
    }

    public List<PfCarga> getPfBatches() {
        return pfCargaRepository.findAll();
    }

    @Transactional
    public void clearData() {
        dhCargaRepository.deleteAll();
        pfCargaRepository.deleteAll();
        notifications.clear();
        addNotification("History and databases cleared successfully.", "INFO");
    }
}
