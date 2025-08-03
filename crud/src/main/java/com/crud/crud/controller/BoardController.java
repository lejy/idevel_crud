package com.crud.crud.controller;

import com.crud.crud.entity.Board;
import com.crud.crud.entity.FileSystem;
import com.crud.crud.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Controller
@RequestMapping("/crudBoard")
@RequiredArgsConstructor
public class BoardController {

    private final BoardRepository boardRepository;


    @Value("${file.upload-dir}")
    private String uploadDir;

    // 게시글 등록
    @GetMapping("/write")
    public String writeForm() {
        return "write"; // write.html
    }


    // 게시글 등록 처리
    @PostMapping("/write")
    public String createPost(@RequestParam String title,
                             @RequestParam String content,
                             @RequestParam String name,
                             @RequestParam String filename,
                             @RequestPart(required = false) List<MultipartFile> files) throws IOException {

        Board post = new Board();
        post.setTitle(title);
        post.setContent(content);
        post.setName(name);

        List<FileSystem> attachments = new ArrayList<>();

        if (files != null) {
            int count = 0;
            for (MultipartFile file : files) {
                String original;

                if (filename == null || filename.trim().isEmpty()) {
                    original = file.getOriginalFilename();
                } else {

                    String[] nameparts = filename.split(",");
                    original = nameparts[count];

                    //original = filename;
                }

                String stored = UUID.randomUUID() + "_" + original;
                Path savePath = Paths.get(uploadDir, stored);
                Files.createDirectories(savePath.getParent());
                Files.copy(file.getInputStream(), savePath, StandardCopyOption.REPLACE_EXISTING);

                FileSystem attachment = new FileSystem();
                attachment.setOriginalName(original);
                attachment.setStoredName(stored);
                attachment.setPath(savePath.toString());
                attachment.setBoard(post);

                attachments.add(attachment);
                count = count + 1;
            }
        }

        post.setFile(attachments);
        boardRepository.save(post);

        return "redirect:/crudBoard/view";
    }

    //게시글 수정 폼
    @GetMapping("/update/{id}")
    public String updatePost(@PathVariable Long id, Model model) {
        Board board = boardRepository.findById(id).orElseThrow();
        model.addAttribute("post", board);
        return "update";
    }


    // 게시글 수정 처리
    @PostMapping("/update")
    public String updatePost(@RequestParam Long id,
                             @RequestParam String title,
                             @RequestParam String content,
                             @RequestParam String name){
        Board board = boardRepository.findById(id).orElseThrow();
        board.setTitle(title);
        board.setContent(content);
        board.setName(name);
        boardRepository.save(board);

        return "redirect:/crudBoard/post/" + id;

    }


    // 게시글 전체 목록 조회
    @GetMapping("/view")
    public String ViewPosts(Model model) {
        List<Board> posts = boardRepository.findAll();
        model.addAttribute("posts", posts);
        return "board";
    }


    // 게시글 단건 조회
    @GetMapping("/post/{id}")
    public String getPost(@PathVariable Long id, Model model) {
        Board post = boardRepository.findById(id).orElseThrow();
        model.addAttribute("post", post);
        return "detail";
    }

    // 게시글 삭제
    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable Long id) throws IOException {
        Board post = boardRepository.findById(id).orElseThrow();
        for (FileSystem file : post.getFile()) {
            Files.deleteIfExists(Paths.get(file.getPath()));
        }
        boardRepository.delete(post);
        return "redirect:/crudBoard/view";
    }

}
