package io.student.pet.repository;

import io.student.pet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Locale;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<Integer> findByUsername(String username);
}