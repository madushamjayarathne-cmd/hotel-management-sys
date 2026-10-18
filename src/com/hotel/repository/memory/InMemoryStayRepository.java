package com.hotel.repository.memory;

import com.hotel.model.Stay;
import com.hotel.repository.StayRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SequencedCollection;

public class InMemoryStayRepository implements StayRepository {
    private final List<Stay> stays;
    private long lastId;

    public InMemoryStayRepository(SequencedCollection<Stay> initial) {
        stays = new ArrayList<>(initial);
        lastId = stays.stream().mapToLong(Stay::id).max().orElse(0);
    }

    @Override
    public synchronized Stay save(Stay stay) {
        if (stay.id() == 0) {
            Stay created = stay.withId(++lastId);
            stays.add(created);
            return created;
        }
        stays.replaceAll(s -> s.id() == stay.id() ? stay : s);
        return stay;
    }

    @Override
    public synchronized Optional<Stay> findActiveByRoom(int roomNumber) {
        return stays.stream().filter(s -> s.roomNumber() == roomNumber && s.active()).findFirst();
    }

    @Override
    public synchronized List<Stay> findActive() {
        return stays.stream().filter(Stay::active).toList();
    }

    @Override
    public synchronized List<Stay> findByRoom(int roomNumber) {
        return stays.stream().filter(s -> s.roomNumber() == roomNumber).toList();
    }

    protected synchronized List<Stay> snapshot() {
        return List.copyOf(stays);
    }
}
