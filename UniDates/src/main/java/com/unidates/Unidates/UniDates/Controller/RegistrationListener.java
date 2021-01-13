package com.unidates.Unidates.UniDates.Controller;

import com.unidates.Unidates.UniDates.Model.Entity.GestioneUtente.Utente;
import com.unidates.Unidates.UniDates.Service.GestioneUtenti.UtenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.UUID;

@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {
    @Autowired
    private UtenteService service;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.confirmRegistration(event);
    }

    private void confirmRegistration(OnRegistrationCompleteEvent event) {
        Utente utente = event.getUtente();
        String token = UUID.randomUUID().toString();
        service.createVerificationToken(utente, token);

        String emailDestinatario = utente.getEmail();
        String oggetto = "Conferma la registrazione";
        String linkDiConferma = event.getAppUrl() + "/api/UserManager/registrationConfirm?token=" + token;
        //String messaggio = messageSource.getMessage("message.regSuc", null, event.getLocale());

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(emailDestinatario);
        email.setSubject(oggetto);
        email.setText("http://localhost:8080" + linkDiConferma);
        System.out.println("http://localhost:8080" + linkDiConferma);
        mailSender.send(email);
    }

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        mailSender.setUsername("unidates.confirm@gmail.com");
        mailSender.setPassword("UnidatesConfirm@@");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }
}
