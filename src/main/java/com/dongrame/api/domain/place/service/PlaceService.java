package com.dongrame.api.domain.place.service;

import com.dongrame.api.domain.bookmark.dao.BookmarkRepository;
import com.dongrame.api.domain.place.dao.LocationRepository;
import com.dongrame.api.domain.place.dao.PlaceRepository;
import com.dongrame.api.domain.place.dao.SearchRepository;
import com.dongrame.api.domain.place.dto.LocationResponseDTO;
import com.dongrame.api.domain.place.dto.PlaceInfoResponseDTO;
import com.dongrame.api.domain.place.dto.SearchPlaceRequestDTO;
import com.dongrame.api.domain.place.dto.SearchPlaceResponseDTO;
import com.dongrame.api.domain.place.entity.Location;
import com.dongrame.api.domain.place.entity.Place;
import com.dongrame.api.domain.place.entity.Search;
import com.dongrame.api.domain.review.entity.Score;
import com.dongrame.api.domain.user.entity.User;
import com.dongrame.api.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final LocationRepository locationRepository;
    private final SearchRepository searchRepository;
    private final UserService userService;
    private final BookmarkRepository bookmarkRepository;

    @Transactional
    public Long saveSearchPlace(SearchPlaceRequestDTO dto) {
        User currentUser = userService.getCurrentUser();
        Location location = locationRepository.findByLatitudeAndLongitude(dto.getLatitude(), dto.getLongitude());
        if(location == null) {
            Place newPlace = saveNewPlaceAndLocation(dto);
            saveNewSearch(currentUser, newPlace);
            return newPlace.getId();
        }
        Place place = placeRepository.findById(location.getId())
                .orElseThrow(() -> new RuntimeException("해당 장소는 존재하지 않습니다."));
        Search existingSearch = searchRepository.findByUserAndPlace(currentUser, place);
        if(existingSearch != null) {
            searchRepository.delete(existingSearch);
        }
        saveNewSearch(currentUser, place);

        return place.getId();
    }

    private void saveNewSearch(User currentUser, Place newPlace) {
        Search search = Search.toSearch(currentUser, newPlace);
        searchRepository.save(search);
    }

    private Place saveNewPlaceAndLocation(SearchPlaceRequestDTO dto) {
        Place newPlace = Place.toPlace(dto);
        Location newLocation = Location.toLocation(dto, newPlace);
        newPlace.updateLocation(newLocation);
        placeRepository.save(newPlace);
        return newPlace;
    }

    public List<SearchPlaceResponseDTO> getSearchPlace() {
        User currentUser = userService.getCurrentUser();
        List<Search> searches = searchRepository.findByUserOrderByCreatedAtDesc(currentUser);
        return searches.stream()
                .map(Search::getPlace)
                .map(SearchPlaceResponseDTO::toSearchPlaceResponseDTO)
                .toList();
    }

    public PlaceInfoResponseDTO getPlaceInfo(Long placeId){
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new RuntimeException("장소를 찾을 수 없습니다"));
        User currentUser = userService.getCurrentUser();
        boolean isBookmarked = bookmarkRepository.findByUserAndPlace(currentUser, place).isPresent();
        Integer GOOD = place.getGOOD();
        Integer SOSO = place.getSOSO();
        Integer BAD = place.getBAD();

        Integer maxScore = GOOD; // 초기값을 GOOD으로 설정
        Score maxCategory = Score.GOOD; // 초기 우선 순위

        if (SOSO > maxScore) {
            maxScore = SOSO;
            maxCategory = Score.SOSO;
        }
        if (BAD > maxScore) {
            maxCategory = Score.BAD;
        }

        return PlaceInfoResponseDTO.toInfoResponseDTO(place, isBookmarked,maxCategory);
    }

    public LocationResponseDTO getLocation(Long placeId) {
        Location location = locationRepository.findById(placeId)
                .orElseThrow(() -> new RuntimeException("장소를 찾을 수 없습니다"));
        return LocationResponseDTO.toLocationResponseDTO(location);
    }
}
