package com.crud.crud.controller;

import com.crud.crud.entity.Board;
import com.crud.crud.entity.FileSystem;
import com.crud.crud.repository.BoardRepository;
import com.crud.crud.repository.FileSystemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/crudBoard")
@RequiredArgsConstructor
public class BoardController {

    private final BoardRepository boardRepository;
    private final FileSystemRepository fileSystemRepository;


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

    //파일다운로드
    @GetMapping("/download/file/{originalName}")
    public ResponseEntity<Resource> downloadFileById(@PathVariable("originalName") String originalName) throws IOException {
        FileSystem file = (FileSystem) fileSystemRepository.findByOriginalName(originalName).orElseThrow();


        Path filePath = Paths.get(file.getPath());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new FileNotFoundException("파일을 찾을 수 없거나 읽을 수 없습니다.");
        }

        String encodedName = URLEncoder.encode(file.getOriginalName(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedName + "\"")
                .body(resource);
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
