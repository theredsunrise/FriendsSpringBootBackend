package com.example.demo.user.infrastructure.out.repository;


import com.example.demo.shared.application.port.in.Page;
import com.example.demo.shared.application.port.in.PageResult;
import com.example.demo.shared.infrastructure.out.openTelemetry.ObservationPredicateConfig;
import com.example.demo.shared.infrastructure.out.cache.CacheNames;
import com.example.demo.user.application.port.out.repository.UserRepository;
import com.example.demo.user.domain.User;
import com.example.demo.user.infrastructure.out.repository.jpa.SpringDataUserRepository;
import com.example.demo.user.infrastructure.out.repository.jpa.dto.UserJpaEntity;
import com.example.demo.user.infrastructure.out.repository.jpa.maper.UserJpaMapper;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserAdapter implements UserRepository {

    private final UserJpaMapper mapper;
    private final SpringDataUserRepository repository;

    @Override
    @Observed(name = "user-save", contextualName = "user-save",
            lowCardinalityKeyValues = {ObservationPredicateConfig.SKIP_DB_KEY, "true"})
    public User save(User user) {
        UserJpaEntity dto = mapper.toJpaEntity(user);
        UserJpaEntity savedEntity = repository.save(dto);
        return mapper.toDomain(savedEntity);
    }

    @Override
    @Observed(name = "user-get-id", contextualName = "user-get-id")
    @Cacheable(value = CacheNames.USER_BY_ID, key = "#a0")
    public Optional<User> getById(UUID userId) {
        return repository.findById(userId).map(mapper::toDomain);
    }

    @Override
    @CacheEvict(value = CacheNames.USER_BY_ID, key = "#a0")
    @Observed(name = "user-delete-id", contextualName = "user-delete-id")
    public void deleteById(UUID userId) {
        repository.deleteById(userId);
    }

    @Override
    @Observed(name = "user-exist-id", contextualName = "user-exist-id")
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    @Observed(name = "user-exist-username", contextualName = "user-exist-username")
    public boolean existsByUsername(String username) {
        return repository.existsByUsername(username);
    }

    @Override
    @Observed(name = "user-find-all", contextualName = "user-find-all")
    public PageResult<User> findAll(Page page) {
        ScrollPosition scrollPosition = page.parseTimeAndIdScrollPosition(Page.Keys.CREATED_AT_AND_ID);
        Window<UserJpaEntity> result = repository.findByOrderByCreatedAtDescIdAsc(
                scrollPosition,
                Limit.of(page.size()));

        List<User> users = result.map(mapper::toDomain).toList();
        User lastUser = users.isEmpty() ? null : users.getLast();
        int size = users.size();

        Page nextPage = (result.hasNext() && lastUser != null)
                ? new Page("%s_%s".formatted(lastUser.getCreatedAt(), lastUser.getId()), size)
                : new Page(null, size);
        return new PageResult<>(
                users,
                page,
                nextPage
        );
    }
}
