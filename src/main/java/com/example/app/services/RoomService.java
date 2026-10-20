package com.example.app.services;
import com.example.app.dto.room.input.RoomRequest;
import com.example.app.dto.room.output.RoomResponse;
import com.example.app.dto.PageResponse;
import com.example.app.dto.ReservationStatus;
import com.example.app.models.main.*;
import com.example.app.repos.main.*;
import com.example.app.exceptions.*;
import com.example.app.mappers.RoomMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;
import java.util.*;
@Service @RequiredArgsConstructor
public class RoomService {

    private final RoomRepository rooms;

    private final UserRepository users;

    @Transactional
    public RoomResponse createRoom(
        RoomRequest dto,
        UUID userId
    ) {
        Room room=new Room();

        room.setOwner(users.findById(userId).orElseThrow(()->new UserNotFoundException("User not found")));

        RoomMapper.apply(room,dto);

        return RoomMapper.toDto(rooms.saveAndFlush(room));
    }
    @Transactional
    public RoomResponse updateRoom(
        UUID id,
        RoomRequest dto,
        UUID userId
    ) {
        Room room = owned(id,userId);
        
        RoomMapper.apply(room,dto);

        return RoomMapper.toDto(rooms.saveAndFlush(room));
    }

    @Transactional(readOnly=true)
    public RoomResponse getRoomById(
        UUID id
    ) {
        return RoomMapper.toDto(rooms.findById(id).orElseThrow(() -> new RoomNotFoundException("Room not found")));
    }
    @Transactional public void deleteRoom(
        UUID id,UUID userId
    ) {
        rooms.delete(owned(id,userId));
        
        rooms.flush();
    }

    private Room owned(
        UUID id,
        UUID userId
    ) {
        Room room = rooms.findByIdForUpdate(id).orElseThrow(() -> new RoomNotFoundException("Room not found"));

        if (!room.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Room belongs to another user");
        }

        return room;
    }

    @Transactional(readOnly=true)
    public PageResponse<RoomResponse> search(
        int page,
        int size,
        String sort,
        String direction,
        String homeType,
        String address,
        Long minPrice,
        Long maxPrice,
        Boolean internet,
        LocalDate start,
        LocalDate end
    ) {
        if (page < 0 || size < 1 || size > 100 || !Set.of("id", "price", "address", "homeType").contains(sort)) {
            throw new IllegalArgumentException("Invalid pagination or sort");
        }

        if (!Set.of("asc","desc").contains(direction.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Sort direction must be asc or desc");
        }
        
        if ((minPrice != null && minPrice < 0) || (maxPrice != null && maxPrice < 0) || (minPrice!=null && maxPrice!=null && minPrice>maxPrice)) {
            throw new IllegalArgumentException("Invalid price range");
        }
        
        if ((start==null)!=(end==null) || (start!=null && (start.isBefore(LocalDate.now()) || !start.isBefore(end)))) {
            throw new IllegalArgumentException("Supply a valid start/end date pair");
        }
        
        Specification<Room> filter = (root,query,cb) -> {
            List<Predicate> p=new ArrayList<>();
            
            if (homeType!=null) {
                p.add(cb.equal(root.get("homeType"), homeType));
            }
            if (address!=null) {
                p.add(cb.like(cb.lower(root.get("address")), "%" + address.toLowerCase(Locale.ROOT).replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%", '\\'));
            }
            if (minPrice!=null) {
                p.add(cb.ge(root.get("price"), minPrice));
            }
            if (maxPrice!=null) {
                p.add(cb.le(root.get("price"), maxPrice));
            }
            if (internet!=null) {
                p.add(cb.equal(root.get("hasInternet"), internet));
            }

            if (start != null) {
                var occupied = query.subquery(UUID.class);

                var r = occupied.from(Reservation.class);

                occupied.select(r.get("id")).where(cb.equal(r.get("room"), root), cb.notEqual(r.get("status"),ReservationStatus.CANCELED), cb.lessThan(r.get("startDate"),end),cb.greaterThan(r.get("endDate"),start));
                
                p.add(cb.not(cb.exists(occupied)));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
        Sort ordering=Sort.by(Sort.Direction.fromString(direction),sort);

        if (!sort.equals("id")) {
            ordering = ordering.and(Sort.by("id"));
        }

        return PageResponse.from(rooms.findAll(filter, PageRequest.of(page,size,ordering)).map(RoomMapper::toDto));
    }
}
