package org.ngelmakproject.service;

import org.ngelmakproject.domain.Donation;
import org.ngelmakproject.repository.DonationRepository;
import org.ngelmakproject.web.rest.dto.DonationStats;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DonationService {

    private final DonationRepository repository;

    public DonationService(DonationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Donation createDonation(Donation request, String currentUserName, String currentUserId) {
        if (request.getAmount() == null || request.getAmount() < 0) {
            throw new IllegalArgumentException("Montant invalide");
        }

        Donation donation = new Donation();
        donation.setAmount(request.getAmount());
        donation.setMessage(request.getMessage());

        // if (!request.isAnonymous()) {
        // // Si l'utilisateur veut afficher son nom
        // String nameToDisplay = request.getDisplayName();
        // if (nameToDisplay == null || nameToDisplay.isBlank()) {
        // nameToDisplay = currentUserName; // si tu as un système d'auth
        // }
        // donation.setDisplayName(nameToDisplay);
        // } else {
        // donation.setDisplayName(null);
        // }

        // if (currentUserId != null) {
        // donation.setUserId(java.util.UUID.fromString(currentUserId));
        // }

        return repository.save(donation);
    }

    @Transactional(readOnly = true)
    public List<Donation> getLastDonations() {
        return repository.findTop20ByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public DonationStats getStats() {
        var all = repository.findAll();
        var total = all.stream()
                .map(Donation::getAmount)
                .reduce(0, (subtotal, element) -> subtotal + element);

        int count = all.size();
        Integer average = count == 0 ? 0 : total / count;

        Donation last = all.stream()
                .max((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                .orElse(null);

        DonationStats stats = new DonationStats();
        stats.setTotalAmount(total);
        stats.setCount(count);
        stats.setAverageAmount(average);
        stats.setLastDonationAmount(last != null ? last.getAmount() : null);

        return stats;
    }
}