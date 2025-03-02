package com.agence.annonce.web.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.tomcat.jni.Library;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.agence.annonce.dao.entities.Annonce;
import com.agence.annonce.dao.entities.Category;
import com.agence.annonce.dao.entities.Photo;

import com.agence.annonce.web.models.annonceForm;

import jakarta.validation.Valid;

import com.agence.annonce.business.services.AddresseService;
import com.agence.annonce.business.services.AnnonceService;
import com.agence.annonce.business.services.PhotoService;
import com.agence.annonce.dao.entities.Address;

import org.springframework.mock.web.MockMultipartFile;



@Controller
@RequestMapping("/annonces")
public class AnnonceController  {

    private static List<Annonce> annonces = new ArrayList<Annonce>();
    private static Long idCount =0L;
    public static String uploadDirectory = System.getProperty("user.dir") + "/src/main/resources/static/images";

    private final AnnonceService annonceService;
    private final AddresseService addresseService;
    private final PhotoService photoService;
    public AnnonceController( AnnonceService annonceService,AddresseService addresseService, PhotoService photoService){

        this.annonceService=annonceService;
        this.addresseService=addresseService;
        this.photoService=photoService;
    }
       
    @RequestMapping("/property-list")
    public String getAllproduct(Model model) {
        List<Annonce> annonces = annonceService.getAllAnnonce();
        model.addAttribute("annonces", annonces);
        
        return "property-list";
    }


    @GetMapping("/create-property")
    public String showAddProperty(Model model) {
        model.addAttribute("annonceForm", new annonceForm());
        model.addAttribute("categories", Category.values());
        return "add-property";
    }



     @RequestMapping(path="/create-property", method= RequestMethod.POST)
    public String addProperty(@Valid @ModelAttribute annonceForm annonceForm, BindingResult bindingResult,Model model,@RequestParam("photos") MultipartFile[] photos){
        Address address = new Address(annonceForm.getGovernorate(),annonceForm.getCity(),annonceForm.getStreet());
        this.addresseService.addAddress(address);   


        List<Photo> photosList = new ArrayList<Photo>();
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Invalid input");
            model.addAttribute("categories", Category.values());
            return "add-property";
        }


           Annonce annonce =new Annonce(null, annonceForm.getTitre(), annonceForm.getDescription(), annonceForm.getSurface(), annonceForm.getPrice(), annonceForm.getType(), annonceForm.getCategory(), address, annonceForm.getTel(), null);
                for (MultipartFile photo : photos) {
                    if (!photo.isEmpty()) { // Check if the photo is not empty
                        StringBuilder fileName = new StringBuilder();
                        fileName.append(photo.getOriginalFilename());
                        Path newFilePath = Paths.get(uploadDirectory, fileName.toString());
                        try {
                            Files.write(newFilePath, photo.getBytes());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        photosList.add(new Photo(null, fileName.toString(), annonce));
                    }
                }
                if (!photosList.isEmpty()) {
                    annonce.setPhotos(photosList);
                }
            this.annonceService.addAnnonce(annonce); // Save the annonce, which will also save the photos due to cascade
            return "redirect:/annonces/property-list";
    }
    public MultipartFile convertPhotoToMultipartFile(Photo photo) throws IOException {
        // Check if the url points to a file on disk
        File file = new File(photo.getUrl());
    
        // If file exists and is not a directory
        if (file.exists() && file.isFile()) {
            // Read the file content
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] data = fileInputStream.readAllBytes();
            fileInputStream.close();
    
            // Create the MultipartFile from the file data
            return new MockMultipartFile(
                "file",                           // Form field name
                file.getName(),                    // Original file name
                "image/jpeg",                      // Content type (adjust if necessary)
                data                               // File content as byte array
            );
        } else {
            // Handle the case where the file doesn't exist or the URL is not a valid file
            throw new IOException("File not found at URL: " + photo.getUrl());
        }
    }
 
    @RequestMapping("{id}/edit-property")
    public String showEditProperty(@PathVariable Long id,Model model) {
        Annonce annonce = this.annonceService.getAnnoncebyId(id);
        List<MultipartFile> photoFiles = new ArrayList<>();
        for (Photo photo : annonce.getPhotos()) {
            try {
                MultipartFile photoFile = convertPhotoToMultipartFile(photo);
                model.addAttribute("photoFile", photoFile);
                photoFiles.add(photoFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        model.addAttribute("annonceForm", new annonceForm(annonce.getTitre(),annonce.getType(),annonce.getCategory(), annonce.getDescription(),annonce.getTel(), annonce.getSurface(), annonce.getPrice(), annonce.getAddress().getGovernorate(), annonce.getAddress().getCity(), annonce.getAddress().getStreet(),photoFiles));
        model.addAttribute("annonce_id", id);
        model.addAttribute("categories", Category.values());
        return "edit-property";
    }

   
    @RequestMapping(path = "{id}/edit-property", method = RequestMethod.POST)
    public String editProperty(
        @Valid @ModelAttribute annonceForm annonceForm,
        BindingResult bindingResult,
        @PathVariable Long id,
        Model model,
        @RequestParam("photos") MultipartFile[] photos) {

    if (bindingResult.hasErrors()) {
        model.addAttribute("categories", Category.values());
        return "edit-property";
    }

    // Fetch the existing annonce and its related address and photos
    Annonce annonce = annonceService.getAnnoncebyId(id);
    Address address = annonce.getAddress();

    // Update address details
    address.setGovernorate(annonceForm.getGovernorate());
    address.setCity(annonceForm.getCity());
    address.setStreet(annonceForm.getStreet());

    // Update annonce details
    annonce.setTitre(annonceForm.getTitre());
    annonce.setDescription(annonceForm.getDescription());
    annonce.setSurface(annonceForm.getSurface());
    annonce.setPrice(annonceForm.getPrice());
    annonce.setType(annonceForm.getType());
    annonce.setCategory(annonceForm.getCategory());
    annonce.setTel(annonceForm.getTel());

    // Update photos (modify existing collection instead of replacing it)
    List<Photo> existingPhotos = annonce.getPhotos();
    if (existingPhotos == null) {
        existingPhotos = new ArrayList<>();
        annonce.setPhotos(existingPhotos);
    }

    // Add new photos
    for (MultipartFile photo : photos) {
        if (!photo.isEmpty()) {
            String fileName = photo.getOriginalFilename();
            Path filePath = Paths.get(uploadDirectory, fileName);
            try {
                Files.write(filePath, photo.getBytes());
                existingPhotos.add(new Photo(null, fileName, annonce)); // Add to the existing collection
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Save updated entities
    addresseService.updateAddress(address);
    annonceService.updateAnnonce(annonce);

    return "redirect:/annonces/property-list";
}


    @RequestMapping(path = "{id}/delete", method = RequestMethod.POST)
    public String deleteAnnonce(@PathVariable Long id) {
        Annonce annonce = this.annonceService.getAnnoncebyId(id);
        this.addresseService.deleteAddressByAnnonce(annonce);

        List<Photo> photosList = this.photoService.getPhotoByAnnonce(annonce);
        for (Photo photo : photosList) {
            Path filePath = Paths.get(uploadDirectory, photo.getUrl());
            try {
                Files.deleteIfExists(filePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.photoService.deletePhotoByAnnonce(annonce);
        }

            return "redirect:/annonces/property-list";
        }
      
    }

    
    

