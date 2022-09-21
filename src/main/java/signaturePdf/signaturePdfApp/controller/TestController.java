package signaturePdf.signaturePdfApp.controller;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import signaturePdf.signaturePdfApp.Logging;
import signaturePdf.signaturePdfApp.model.FileEdit;
import signaturePdf.signaturePdfApp.model.FileUploadResponse;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Controller
public class TestController {

    public static HashMap<Integer, FileEdit> streamMap = new HashMap<Integer, FileEdit>();

    @Value("${signature1}")
    private String signature1;

    @Value("${signature2}")
    private String signature2;

    @Value("${url}")
    private String url;

    @Value("${deleteTimeInHours}")
    private Long deleteTimeInHours;

    @GetMapping("/upload")
    public String upload(Model model) {
        Integer random = new Random().nextInt(100000000, 999999999);
        model.addAttribute("link", random);
        model.addAttribute("url", url);
        return "upload";
    }

    @GetMapping("/urls/{id}")
    public String urls(@PathVariable Integer id, Model model) {
        model.addAttribute("firstLink", url + "/first/" + id);
        model.addAttribute("secondLink", url + "/second/" + id);
        model.addAttribute("url", url);
        return "urls";
    }

    @GetMapping("/first/{id}")
    public String startFirst(@PathVariable Integer id, Model model) {
        model.addAttribute("link", id);
        model.addAttribute("url", url);
        return "index";
    }

    @GetMapping("/second/{id}")
    public String startSecond(@PathVariable Integer id, Model model) {
        model.addAttribute("link", id);
        model.addAttribute("url", url);
        return "indexSecond";
    }

    @GetMapping("/download/{id}")
    public String download(@PathVariable Integer id, Model model) {
        model.addAttribute("link", id);
        model.addAttribute("url", url);

        System.out.println(streamMap.get(id).firstSignature + "     " + streamMap.get(id).secondSignature);

        if (streamMap.get(id).firstSignature == 1 && streamMap.get(id).secondSignature == 1) {
            model.addAttribute("signed", 1);
        } else {
            model.addAttribute("signed", 0);
        }

        return "download";
    }

    @PostMapping("/receivePdf/{id}")
    public ResponseEntity<FileUploadResponse> uploadFiles(@PathVariable Integer id, @RequestParam("file") MultipartFile multipartFile) {

        FileEdit fileEdit = new FileEdit();
        fileEdit.signature1 = this.signature1;
        fileEdit.signature2 = this.signature2;

        streamMap.put(id, fileEdit);
        Logging.logger.info("File was added to hashmap with id: " + id);

        //delete po nastavenej dobe----------------------------------------
        TimerTask task = new TimerTask() {
            public void run() {
                System.out.println("File was deleted with id: " + id);
                streamMap.remove(id);
            }
        };
        Timer timer = new Timer("Timer");
        long delay = deleteTimeInHours * 1000 * 3600;
        timer.schedule(task, delay);
        //----------------------------------------------------------------

        String fileName = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        long size = multipartFile.getSize();

        try {
            streamMap.get(id).convertFile(multipartFile);
        } catch (IOException ex) {
            Logging.logger.info("ERROR: Input PDF not found:\n");
            ex.printStackTrace();
            System.exit(1);
        }

        FileUploadResponse response = new FileUploadResponse();
        response.setFileName(fileName);
        response.setSize(size);
        Logging.logger.info("PDF was received: " + fileName + "  size: " + size + " bytes");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/receiveImageOne/{id}")
    public ResponseEntity<FileUploadResponse> uploadImageOne(@PathVariable Integer id, @RequestParam("file") MultipartFile multipartFile) {

        System.out.println(id);

        String fileName = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        long size = multipartFile.getSize();

        Logging.logger.info("Signature image was received: " + fileName + "  , size: " + size + " bytes");

        try {
            streamMap.get(id).editFile2(multipartFile, 1);
        } catch (IOException ex) {
            Logging.logger.info("ERROR: Image file not found:\n");
            ex.printStackTrace();
            System.exit(1);
        }

        FileUploadResponse response = new FileUploadResponse();
        response.setFileName(fileName);
        response.setSize(size);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/receiveImageTwo/{id}")
    public ResponseEntity<FileUploadResponse> uploadImageTwo(@PathVariable Integer id, @RequestParam("file") MultipartFile multipartFile) {

        String fileName = StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        long size = multipartFile.getSize();

        Logging.logger.info("Signature image was received: " + fileName + "  , size: " + size + " bytes");

        try {
            streamMap.get(id).editFile2(multipartFile, 2);
        } catch (IOException ex) {
            Logging.logger.info("ERROR: Image file not found:\n");
            ex.printStackTrace();
            System.exit(1);
        }

        FileUploadResponse response = new FileUploadResponse();
        response.setFileName(fileName);
        response.setSize(size);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(value = "/downloadPdf/{id}")
    public ResponseEntity<byte[]> getPDF(@PathVariable Integer id) throws IOException {

        try {
            PDDocument document = new PDDocument();

            document = streamMap.get(id).save2();

            if (document == null) {
                Logging.logger.info("ERROR: Document to sign not found.");
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            document.save(byteArrayOutputStream);

            InputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            byte[] bytes = IOUtils.toByteArray(inputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);

            String filename = "output" + id + ".pdf";

            Logging.logger.info("PDF was downloaded: " + filename);

            headers.setContentDispositionFormData(filename, filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);

        } catch (NullPointerException ex) {
            Logging.logger.info("File download time has expired");
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/download/reload/{id}")
    public ResponseEntity getAll(@PathVariable Integer id) {
        Integer signed;

        if (streamMap.get(id).firstSignature == 1 && streamMap.get(id).secondSignature == 1) {
            signed = 1;
        } else {
            signed = 0;
        }

        return new ResponseEntity<>(signed, HttpStatus.OK);
    }




    @GetMapping("/overview")
    public ResponseEntity getOverview() {

        System.out.println(streamMap);


        return new ResponseEntity<>(streamMap, HttpStatus.OK);
    }




}
