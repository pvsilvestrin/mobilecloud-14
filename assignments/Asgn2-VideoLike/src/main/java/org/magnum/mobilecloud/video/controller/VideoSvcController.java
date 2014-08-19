package org.magnum.mobilecloud.video.controller;

import com.google.common.collect.Lists;
import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.management.BadAttributeValueExpException;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.Collection;

/**
 * Created by pvsilvestrin on 18/08/14.
 */
@Controller
public class VideoSvcController {

    @Autowired
    private VideoRepository videos;

    @RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
    public @ResponseBody Video addVideo(@RequestBody Video video) {
        video = videos.save(video);
        return video;
    }

    @RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.GET)
    public @ResponseBody Collection<Video> getVideoList() {
        return Lists.newArrayList(videos.findAll());
    }

    @RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}", method = RequestMethod.GET)
    public @ResponseBody Video getVideoById(@PathVariable("id") long id) {
        Video v = videos.findOne(id);
        if(v == null) throw new ResourceNotFoundException();
        return v;
    }

    @RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}/like", method = RequestMethod.POST)
    public Void likeVideo(@PathVariable("id") long id, Principal p, HttpServletResponse response) {
        Video v = videos.findOne(id);
        if(v == null) response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        else {
            Collection<String> likedBy = v.getLikedBy();
            if(likedBy.contains(p.getName())) response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            else {
                response.setStatus(HttpServletResponse.SC_OK);
                likedBy.add(p.getName());
                v.setLikedBy(likedBy);
                v.setLikes(v.getLikes()+1);
                videos.save(v);
            }
        }
        return null;
    }

    @RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}/unlike", method = RequestMethod.POST)
    public Void unlikeVideo(@PathVariable("id") long id, Principal p, HttpServletResponse response) {
        Video v = videos.findOne(id);
        if(v == null) response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        else {
            Collection<String> likedBy = v.getLikedBy();
            if(!likedBy.contains(p.getName())) response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            else {
                response.setStatus(HttpServletResponse.SC_OK);
                likedBy.remove(p.getName());
                v.setLikedBy(likedBy);
                v.setLikes(v.getLikes()-1);
                videos.save(v);
            }
        }
        return null;
    }

    @RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}/likedby", method = RequestMethod.GET)
    public @ResponseBody Collection<String> getUsersWhoLikedVideo(@PathVariable("id") long id) {
        Video v = videos.findOne(id);
        if(v == null) throw new ResourceNotFoundException();
        return v.getLikedBy();
    }

    @RequestMapping(value=VideoSvcApi.VIDEO_TITLE_SEARCH_PATH, method=RequestMethod.GET)
    public @ResponseBody Collection<Video> findByTitle(
            // Tell Spring to use the "title" parameter in the HTTP request's query
            // string as the value for the title method parameter
            @RequestParam(VideoSvcApi.TITLE_PARAMETER) String title
    ){
        return videos.findByName(title);
    }

    @RequestMapping(value=VideoSvcApi.VIDEO_DURATION_SEARCH_PATH, method=RequestMethod.GET)
    public @ResponseBody Collection<Video> findByDurationLessThan(
            @RequestParam(VideoSvcApi.DURATION_PARAMETER) long duration) {
        return videos.findByDurationLessThan(duration);
    }
}