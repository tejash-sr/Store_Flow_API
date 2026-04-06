package com.storeflow.storeflow_api.repository;

import com.storeflow.storeflow_api.entity.User;
import com.storeflow.storeflow_api.entity.UserRole;
import com.storeflow.storeflow_api.entity.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @DataJpaTest tests for UserRepository.
 * Verifies custom queries and DB-level uniqueness constraint on email.
 * Satisfies PDF requirement P2-5 (missing repository tests).
 */
@Transactional
class UserRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User buildUser(String email, String name) {
        return User.builder()
                .email(email)
                .password("hashed_password")
                .fullName(name)
                .roles(Set.of(UserRole.ROLE_USER))
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    void saveAndFindByEmail_returnsUser() {
        userRepository.save(buildUser("alice@example.com", "Alice"));

        Optional<User> found = userRepository.findByEmailIgnoreCase("alice@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("Alice");
    }

    @Test
    void findByEmailIgnoreCase_isCaseInsensitive() {
        userRepository.save(buildUser("BOB@EXAMPLE.COM", "Bob"));

        Optional<User> found = userRepository.findByEmailIgnoreCase("bob@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("Bob");
    }

    @Test
    void existsByEmailIgnoreCase_returnsTrueWhenPresent() {
        userRepository.save(buildUser("carol@example.com", "Carol"));

        assertThat(userRepository.existsByEmailIgnoreCase("carol@example.com")).isTrue();
        assertThat(userRepository.existsByEmailIgnoreCase("CAROL@EXAMPLE.COM")).isTrue();
        assertThat(userRepository.existsByEmailIgnoreCase("nobody@example.com")).isFalse();
    }

    @Test
    void emailUniqueness_databaseConstraintPrevencesDuplicates() {
        userRepository.saveAndFlush(buildUser("dup@example.com", "First"));

        assertThatThrownBy(() ->
                userRepository.saveAndFlush(buildUser("dup@example.com", "Second")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findByPasswordResetToken_returnsMatchingUser() {
        User user = buildUser("dave@example.com", "Dave");
        user.setPasswordResetToken("reset-token-abc");
        userRepository.save(user);

        Optional<User> found = userRepository.findByPasswordResetToken("reset-token-abc");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("dave@example.com");
    }

    @Test
    void isResetTokenValid_returnsFalseWhenNoToken() {
        User user = buildUser("eve@example.com", "Eve");
        assertThat(user.isResetTokenValid()).isFalse();
    }
}
