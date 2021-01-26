package com.unidates.Unidates.UniDates.View.component;

import com.unidates.Unidates.UniDates.Controller.GestioneInterazioniController;
import com.unidates.Unidates.UniDates.Controller.GestioneModerazioneController;
import com.unidates.Unidates.UniDates.Controller.GestioneUtentiController;
import com.unidates.Unidates.UniDates.DTOs.SegnalazioneDTO;
import com.unidates.Unidates.UniDates.DTOs.StudenteDTO;
import com.unidates.Unidates.UniDates.Exception.InvalidReportFormatException;
import com.unidates.Unidates.UniDates.Security.SecurityUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;


public class Card_Utente_Home_Component extends Div {

    GestioneInterazioniController gestioneInterazioniController;
    GestioneModerazioneController gestioneModerazioneController;
    GestioneUtentiController gestioneUtentiController;

    public Card_Utente_Home_Component(GestioneUtentiController gestioneUtentiController, GestioneInterazioniController gestioneInterazioniController, StudenteDTO studenteDTO, GestioneModerazioneController gestioneModerazioneController){
        this.gestioneInterazioniController = gestioneInterazioniController;
        this.gestioneUtentiController = gestioneUtentiController;
        this.gestioneModerazioneController = gestioneModerazioneController;
        HorizontalLayout tot = Card(studenteDTO);
        add(tot);
    }


    public HorizontalLayout Card(StudenteDTO studente){
        //layput padre
        HorizontalLayout contenitore = new HorizontalLayout();
        contenitore.setSpacing(false);

        //layout sinistra con foto e pulsanti
        VerticalLayout layout_foto = new VerticalLayout();
        HorizontalLayout pulsanti = new HorizontalLayout();
        StreamResource resource = new StreamResource("ciao",()-> new ByteArrayInputStream(studente.getProfilo().getFotoProfilo().getImg()));
        Image image_profilo = new Image(resource,"");
        image_profilo.getStyle().set("width","250px");
        image_profilo.getStyle().set("height","250px");
        Button like = getLikeButton(studente);
        Button report = reportButton(studente);
        pulsanti.add(like,report);
        layout_foto.add(image_profilo,pulsanti);

        //layout destra con nome interessi e topics
        VerticalLayout layout_info = new VerticalLayout();
        HorizontalLayout nome_cognome = new HorizontalLayout();
        Span nome = new Span(studente.getProfilo().getNome());
        Span cogome = new Span(studente.getProfilo().getCognome());
        nome_cognome.add(nome,cogome);

        Span topics = new Span(studente.getProfilo().getHobbyList().toString());
        Span interessi = new Span("Interessato a:"+studente.getProfilo().getInteressi().toString());
        layout_info.add(nome_cognome,topics,interessi);

        contenitore.add(layout_foto,layout_info);
        return contenitore;
    }


    private Button getLikeButton(StudenteDTO studente) {
        Button like = new Button(new Icon(VaadinIcon.HEART));
        like.getStyle().set("color","white");
        like.addClickListener((buttonClickEvent)->{
            Style style = buttonClickEvent.getSource().getStyle();
            if(style.get("color").equals("white")) {
                buttonClickEvent.getSource().getStyle().set("color", "red");
                gestioneInterazioniController.aggiungiMatch(gestioneUtentiController.utenteInSessione().getEmail(), studente.getEmail());
            }else {
                style.set("color","white");
            }
        });
        return like;
    }

    private Button reportButton(StudenteDTO studente){
        //Notifica Segnalazione
        Notification notifica = new Notification();
        VerticalLayout layout_report = new VerticalLayout();
        TextField reporting = new TextField();
        reporting.setPlaceholder("Inserisci moticazione segnalazione");
        TextArea dettagli = new TextArea();
        dettagli.setPlaceholder("Dettagli segnalazione");
        Button invio = new Button("Invia report",buttonClickEvent -> {
            SegnalazioneDTO segnalazioneDTO = new SegnalazioneDTO(reporting.getValue(), dettagli.getValue());
            try {
                gestioneModerazioneController.inviaSegnalazione(segnalazioneDTO,studente.getProfilo().getFotoProfilo());
            }catch (InvalidReportFormatException c){
                new Notification("Motivazione e/o dettagli non validi.",2000, Notification.Position.MIDDLE);
            }

            notifica.close();
        });
        Button annulla = new Button("Annulla",buttonClickEvent -> {
            notifica.close();
        });
        Button blocca = new Button("Blocca");
        blocca.addClickListener(buttonClickEvent -> {
            gestioneUtentiController.bloccaStudente(gestioneUtentiController.utenteInSessione().getEmail(),studente.getEmail() );
            notifica.close();
            UI.getCurrent().getPage().reload();
        });


        layout_report.setAlignItems(FlexComponent.Alignment.CENTER);
        layout_report.add(reporting,dettagli,invio,blocca,annulla);
        notifica.add(layout_report);
        //Pulsante Report
        Button report = new Button("Report",new Icon(VaadinIcon.PENCIL),buttonClickEvent->{
            notifica.open();
        });
        report.getStyle().set("color","white");
        return report;
    }
}
