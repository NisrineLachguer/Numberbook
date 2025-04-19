package com.lachguer.mobile.controller;

import com.lachguer.mobile.model.Contact;
import com.lachguer.mobile.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.lachguer.mobile.repository.ContactRepository;
import com.lachguer.mobile.repository.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserRepository userRepository;


    @PostMapping
    public ResponseEntity<Contact> createContact(@RequestBody Contact contact, @RequestParam Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String normalizedNumber = contact.getNumber().replaceAll("[^0-9]", "");

        Contact existingContact = contactRepository.findByUserIdAndNumber(userId, normalizedNumber);
        if (existingContact != null) {
            return ResponseEntity.ok(existingContact);
        }

        contact.setNumber(normalizedNumber);
        contact.setUser(user);
        Contact savedContact = contactRepository.save(contact);

        return ResponseEntity.ok(savedContact);
    }

    @GetMapping("/user/{userId}")
    public List<Contact> getContactsByUser(@PathVariable Long userId) {
        return contactRepository.findByUserId(userId);
    }
}