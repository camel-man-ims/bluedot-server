package com.server.bluedotproject.service;

import com.server.bluedotproject.dto.ApiMessage;
import com.server.bluedotproject.dto.request.ArtistHasGenreApiRequest;
import com.server.bluedotproject.dto.response.ArtistApiResponse;
import com.server.bluedotproject.entity.*;
import com.server.bluedotproject.entity.enumclass.RoleName;
import com.server.bluedotproject.entity.repository.*;
import com.server.bluedotproject.exceptions.ErrorCode;
import com.server.bluedotproject.exceptions.NotExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;
    private final UserRepository userRepository;
    private final ArtistHasGenreRepository artistHasGenreRepository;
    private final GenreRepository genreRepository;
    private final UserHasRoleRepository userHasRoleRepository;
    private final RoleRepository roleRepository;

    @Transactional(rollbackFor = {NotExistException.class})
    public Artist createArtist(Long userId, String description, @Nullable String bannerImg, String profileImg, List<String> genreNameList){

        User user = userRepository.findById(userId).orElseThrow(()->new NotExistException(ErrorCode.ARTIST_DOES_NOT_EXIST));

            Artist newArtist = Artist.builder()
                    .id(user.getId())
                    .user(user)
                    .description(description)
                    .profileImg(profileImg)
                    .followedCount(0)
                    .paintNeedAmount(0)
                    .bannerImg(bannerImg)
                    .averageCanvasTime(0)
                    .build();

        Artist registeredArtist = artistRepository.save(newArtist);

        genreNameList.forEach(item -> {
            Genre genre = genreRepository.findByName(item).orElseThrow(() -> new NotExistException(ErrorCode.GENRE_DOES_NOT_EXIST));

            ArtistHasGenre artistHasGenre = ArtistHasGenre.builder()
                    .genre(genre)
                    .artist(registeredArtist)
                    .build();

            artistHasGenreRepository.save(artistHasGenre);
        });


        Role role = roleRepository.findByRoleName(RoleName.ROLE_ARTIST).orElseThrow(()->new NotExistException(ErrorCode.ROLE_NAME_DOES_NOT_EXIST));

        Long userHasRoleId = userHasRoleRepository.findByUser(user).orElseThrow(() -> new NotExistException(ErrorCode.USER_HAS_ROLE_DOES_NOT_EXIST)).getId();

        UserHasRole userHasRole = UserHasRole.builder()
                .id(userHasRoleId)
                .user(user)
                .role(role)
                .build();
        userHasRoleRepository.save(userHasRole);
            return registeredArtist;
    }

    public List<Artist> getAllArtist(Pageable pageable){
        Page<Artist> artistPageList = artistRepository.findAll(pageable);
        return artistPageList.getContent();
    }

    public ApiMessage<List<ArtistApiResponse>> getArtistByGenre(String genre){
        return null;
    }

    public Artist findArtistById(Long id){
        return artistRepository.findById(id).orElseThrow(()->new NotExistException(ErrorCode.ARTIST_DOES_NOT_EXIST));
    }

    public List<ArtistHasGenre> createArtistHasGenre(ArtistHasGenreApiRequest artistHasGenreApiRequest){
        Artist artist = artistRepository.findById(artistHasGenreApiRequest.getArtistId()).orElseThrow(()->new NotExistException(ErrorCode.ARTIST_DOES_NOT_EXIST));
        List<ArtistHasGenre> artistHasGenreList = new ArrayList<>();

        artistHasGenreApiRequest.getGenreList().forEach(item->{

            Genre genre = genreRepository.findByName(item).orElseThrow(()->new NotExistException(ErrorCode.GENRE_DOES_NOT_EXIST));

            ArtistHasGenre artistHasGenre = ArtistHasGenre.builder()
                    .genre(genre)
                    .artist(artist)
                    .build();
            artistHasGenreList.add(artistHasGenreRepository.save(artistHasGenre));
        });
        return artistHasGenreList;
    }

    public Artist getArtist(Long artistId){
        return artistRepository.findById(artistId).orElseThrow(()->new NotExistException(ErrorCode.ARTIST_DOES_NOT_EXIST));
    }



}
