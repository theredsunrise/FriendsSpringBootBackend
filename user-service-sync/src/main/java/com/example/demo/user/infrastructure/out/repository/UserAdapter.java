package com.example.demo.user.infrastructure.out.repository;

import com.example.demo.shared.application.port.in.Page;
import com.example.demo.shared.application.port.in.PageResult;
import com.example.demo.shared.infrastructure.out.cache.CacheNames;
import com.example.demo.shared.infrastructure.out.openTelemetry.ObservationPredicateConfig;
import com.example.demo.user.application.port.out.repository.UserRepository;
import com.example.demo.user.domain.User;
import com.example.demo.user.infrastructure.out.repository.dto.UserMongoEntity;
import com.example.demo.user.infrastructure.out.repository.mapper.UserJpaMapper;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserAdapter implements UserRepository {

    private final SpringDataMongoUserRepository repository;
    private final UserJpaMapper mapper;

    @Override
    @Observed(name = "user-save", contextualName = "user-save",
            lowCardinalityKeyValues = {ObservationPredicateConfig.SKIP_DB_KEY, "true"})
    public User save(User user) {
        UserMongoEntity dto = mapper.toMongoEntity(user);
        UserMongoEntity savedEntity = repository.save(dto);
        return mapper.toDomain(savedEntity);
    }

    @Override
    @Observed(name = "user-exist-id", contextualName = "user-exist-id")
    public boolean existsById(Long userId) {
        return repository.existsByUserId(userId);
    }

    @Override
    @Observed(name = "user-exist-username", contextualName = "user-exist-username")
    public boolean existsByUsername(String username) {
        return repository.existsByUsername(username);
    }

    @Override
    @Observed(name = "user-exist-id", contextualName = "user-exist-id")
    @Cacheable(value = CacheNames.USER_BY_ID, key = "#a0")
    public Optional<User> getById(Long userId) {
        return repository.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    @CacheEvict(value = CacheNames.USER_BY_ID, key = "#a0")
    @Observed(name = "user-delete-id", contextualName = "user-delete-id")
    public void deleteById(Long userId) {
        repository.deleteByUserId(userId);
    }

    @Override
    @Observed(name = "user-find-all", contextualName = "user-find-all")
    public PageResult<User> findAll(Page page) {
        Page.Params params = page.tokenToTimeAndId();

        Slice<UserMongoEntity> result = repository.findAll(
                params == null ? java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE) : params.id(),
                params == null ? Instant.now().plus(1, ChronoUnit.DAYS) : params.time(),
                Pageable.ofSize(page.size()));

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
