package com.unidates.Unidates.UniDates.View.component_pannello_moderazione;

import com.unidates.Unidates.UniDates.Controller.GestioneModerazioneController;
import com.unidates.Unidates.UniDates.Controller.GestioneProfiloController;
import com.unidates.Unidates.UniDates.Controller.GestioneUtentiController;
import com.unidates.Unidates.UniDates.DTOs.AmmonimentoDTO;
import com.unidates.Unidates.UniDates.DTOs.SegnalazioneDTO;
import com.unidates.Unidates.UniDates.DTOs.SospensioneDTO;
import com.unidates.Unidates.UniDates.DTOs.FotoDTO;
import com.unidates.Unidates.UniDates.DTOs.ProfiloDTO;
import com.unidates.Unidates.UniDates.DTOs.ModeratoreDTO;
import com.unidates.Unidates.UniDates.DTOs.StudenteDTO;
import com.unidates.Unidates.UniDates.Exception.InvalidBanFormatException;
import com.unidates.Unidates.UniDates.Exception.InvalidReportFormatException;
import com.unidates.Unidates.UniDates.Exception.InvalidWarningFormatException;
import com.unidates.Unidates.UniDates.Model.Enum.Motivazione;
import com.unidates.Unidates.UniDates.Model.Enum.Ruolo;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.StreamResource;


import java.io.ByteArrayInputStream;

public class ListaSegnalazioni extends VerticalLayout{


    GestioneModerazioneController gestioneModerazioneController;


    GestioneUtentiController gestioneUtentiController;


    GestioneProfiloController gestioneProfiloController;

    ModeratoreDTO moderatore;

    public ListaSegnalazioni(ModeratoreDTO moderatore, GestioneUtentiController gestioneUtentiController, GestioneModerazioneController gestioneModerazioneController, GestioneProfiloController gestioneProfiloController){
        this.gestioneModerazioneController = gestioneModerazioneController;
        this.gestioneUtentiController = gestioneUtentiController;
        this.gestioneProfiloController = gestioneProfiloController;
        this.moderatore = moderatore;
        addAttachListener(event -> createListaSegnalazioni());
    }
    public void createListaSegnalazioni(){
        VerticalLayout layoutListaSegnalazioni = new VerticalLayout();
            for (SegnalazioneDTO segnalazioneDTO : moderatore.getSegnalazioneRicevute()) {
                FotoDTO fotoSegnalata = gestioneProfiloController.trovaFoto(segnalazioneDTO.getFotoId());
                ProfiloDTO profiloSegnalato = gestioneProfiloController.trovaProfilo(fotoSegnalata.getProfiloId());
                StudenteDTO studenteSegnalato =gestioneUtentiController.trovaStudente(profiloSegnalato.getEmailStudente());
                if(fotoSegnalata.isVisible() && !(studenteSegnalato.isBanned())) //da ricontrollare
                    layoutListaSegnalazioni.addComponentAsFirst(cardSegnalazione(fotoSegnalata, profiloSegnalato, segnalazioneDTO, studenteSegnalato));
        }
        add(layoutListaSegnalazioni);
    }

    Notification dettagliSegnalazione = new Notification();
    Notification notificaAmm = new Notification();
    Notification notifica = new Notification();


    public HorizontalLayout cardSegnalazione(FotoDTO fotoSegnalata, ProfiloDTO profiloSegnalato, SegnalazioneDTO segnalazione, StudenteDTO studenteSegnalato){

        HorizontalLayout layoutCard = new HorizontalLayout();
        Image immagineCard = new Image(new StreamResource("stream",()-> new ByteArrayInputStream( fotoSegnalata.getImg())),"Immagine non trovata!");
        immagineCard.getStyle().set("width","150px");
        immagineCard.getStyle().set("height","150px");
        
        Span testoInfoSegnalazione = new Span("Hai ricevuto una segnalazione per una foto di : " + profiloSegnalato.getNome() + profiloSegnalato.getCognome());
        
        //Notifica di dettagli della segnalazione


        Button mostraDettagliSegnalazione = new Button("Mostra dettagli");
        mostraDettagliSegnalazione.addClickListener(buttonClickEvent -> {

            if(!dettagliSegnalazione.isOpened()) {
                dettagliSegnalazione = createNotificaDettagliSegnalazione(segnalazione, fotoSegnalata);
                dettagliSegnalazione.open();
            }
            else dettagliSegnalazione.close();

        });


        Button sospensioneCm = new Button("Invia a CM");
        sospensioneCm.addClickListener(buttonClickEvent -> {

            if(!notifica.isOpened()) {
                notifica = createNotificaInviaACM(fotoSegnalata);
                notifica.open();
            }
            else notifica.close();
        });
        
        VerticalLayout InfoEMostraDettagliLayout = new VerticalLayout();
        InfoEMostraDettagliLayout.add(testoInfoSegnalazione, mostraDettagliSegnalazione);

        Button apriCardAmmonimento = new Button("Ammonimento");
        apriCardAmmonimento.addClickListener(e-> {

            if(!notificaAmm.isOpened()){
                notificaAmm = createNotificaAmmonimento(fotoSegnalata, profiloSegnalato, segnalazione, studenteSegnalato);
                notificaAmm.open();
            }
            else notificaAmm.close();

        });

        layoutCard.setAlignItems(Alignment.CENTER);
        layoutCard.add(immagineCard,InfoEMostraDettagliLayout,apriCardAmmonimento);

        if(moderatore.getRuolo() == Ruolo.COMMUNITY_MANAGER){
            Button apriCardSospensione = new Button("Sospensione");
            apriCardSospensione.setWidth("230px");
            Notification notificaSos = notificaSospensione(fotoSegnalata, profiloSegnalato, studenteSegnalato);
            apriCardSospensione.addClickListener( event ->{
                if(!notificaSos.isOpened())
                           notificaSos.open();
                else notificaSos.close();
            });
            layoutCard.add(apriCardSospensione);
        }
        else{
        layoutCard.add(sospensioneCm);
        }
        return layoutCard;
    }

    private Notification createNotificaDettagliSegnalazione(SegnalazioneDTO segnalazione, FotoDTO fotoSegnalata) {

        Notification dettagliSegnalazione = new Notification();
        Image image = new Image(new StreamResource("ciao", () -> new ByteArrayInputStream(fotoSegnalata.getImg())), "Immagine non trovata");
        image.getStyle().set("width", "250px");
        image.getStyle().set("height", "250px");

        VerticalLayout layoutDettagliSegnalazione = new VerticalLayout();
        layoutDettagliSegnalazione.setAlignItems(Alignment.CENTER);

        Select<String> motivazione = new Select<>();
        Motivazione[] motivaziones =  Motivazione.values();
        motivazione.setItems(motivaziones[0].toString(), motivaziones[1].toString(), motivaziones[2].toString(), motivaziones[3].toString(), motivaziones[4].toString());
        motivazione.setEnabled(false);

        TextField dettagli = new TextField();
        dettagli.setValue(segnalazione.getDettagli());
        dettagli.setEnabled(false);

        Button chiudiDettagliSegnalazione = new Button("Chiudi");
        chiudiDettagliSegnalazione.addClickListener(buttonClickEvent1 -> {
            dettagliSegnalazione.close();
        });


        layoutDettagliSegnalazione.add(image, motivazione, dettagli, chiudiDettagliSegnalazione);
        dettagliSegnalazione.add(layoutDettagliSegnalazione);
        dettagliSegnalazione.setPosition(Notification.Position.MIDDLE);

        return dettagliSegnalazione;
    }

    private Notification createNotificaInviaACM(FotoDTO fotoSegnalata) {
        Notification inviaCM = new Notification();

        Select<String> reporting = new Select<>();
        Motivazione[] motivaziones =  Motivazione.values();
        reporting.setItems(motivaziones[0].toString(), motivaziones[1].toString(), motivaziones[2].toString(), motivaziones[3].toString(), motivaziones[4].toString());

        TextArea dettagli = new TextArea();
        dettagli.setPlaceholder("Dettagli segnalazione");
        Button annulla = new Button("Chiudi");
        annulla.addClickListener(buttonClickEvent1 -> {
            inviaCM.close();
        });

        Button invia = new Button("Invia");
        invia.addClickListener(buttonClickEvent1 -> {
            SegnalazioneDTO segnalazioneDTO = new SegnalazioneDTO(Motivazione.valueOf(reporting.getValue()), dettagli.getValue());
            try {
                gestioneModerazioneController.inviaSegnalazioneCommunityManager(segnalazioneDTO, fotoSegnalata);
            } catch (InvalidReportFormatException c) {
                new Notification("Motivazione e/o dettagli non validi.", 2000, Notification.Position.MIDDLE).open();
            }
            inviaCM.close();
        });

        VerticalLayout layout_report = new VerticalLayout();
        layout_report.setAlignItems(Alignment.CENTER);
        layout_report.add(reporting, dettagli, annulla, invia);
        inviaCM.add(layout_report);
        inviaCM.setPosition(Notification.Position.MIDDLE);

        return inviaCM;
    }


    public Notification createNotificaAmmonimento(FotoDTO fotoSegnalata, ProfiloDTO profiloSegnalato, SegnalazioneDTO segnalazione, StudenteDTO studenteDTO){

        Notification cardAmmonimento = new Notification();

        VerticalLayout layoutCardAmmonimento = new VerticalLayout();
        layoutCardAmmonimento.setAlignItems(FlexComponent.Alignment.CENTER);

        Button chiudiCardAmmonimento = new Button("Annulla");
        chiudiCardAmmonimento.addClickListener(e -> {
            cardAmmonimento.close();
        });

        HorizontalLayout layoutInternoAmmonimento = new HorizontalLayout();
        layoutInternoAmmonimento.add(infoAmmonimento(cardAmmonimento,fotoSegnalata, profiloSegnalato, studenteDTO));
        layoutCardAmmonimento.add(layoutInternoAmmonimento,chiudiCardAmmonimento);

        cardAmmonimento.add(layoutCardAmmonimento);
        cardAmmonimento.setPosition(Notification.Position.MIDDLE);


        return cardAmmonimento;
    }


    public Notification notificaSospensione(FotoDTO fotoSegnalata, ProfiloDTO profiloSegnalato, StudenteDTO studenteSegnalato){
        Notification cardSospensione = new Notification();

        Button annulla = new Button("Annulla");
        annulla.addClickListener(e ->
            cardSospensione.close()
        );

        VerticalLayout layoutCardSospensione = new VerticalLayout();
        layoutCardSospensione.setAlignItems(FlexComponent.Alignment.CENTER);

        Image image = new Image(new StreamResource("ciao",()-> new ByteArrayInputStream(fotoSegnalata.getImg())),"Immagine non trovata");
        image.getStyle().set("width","250px");
        image.getStyle().set("height","250px");

        HorizontalLayout horizontal = new HorizontalLayout();
        horizontal.add(infoSospensione(cardSospensione,profiloSegnalato, studenteSegnalato));
        layoutCardSospensione.add(image,horizontal,annulla);

        cardSospensione.add(layoutCardSospensione);
        cardSospensione.setPosition(Notification.Position.MIDDLE);

        return cardSospensione;
    }

    private HorizontalLayout infoSospensione(Notification cardSospensione, ProfiloDTO profiloSegnalato, StudenteDTO studenteSegnalato) {
        HorizontalLayout layoutInfoSospensione = new HorizontalLayout();

        //layout sinistra
        VerticalLayout layoutSinistraSospensione = new VerticalLayout();

        TextField durata = new TextField("(Inserire un numero)");
        durata.setPlaceholder("Durata sospensione");

        TextField dettagli = new TextField();
        dettagli.setPlaceholder("Dettagli");

        layoutSinistraSospensione.setAlignItems(FlexComponent.Alignment.CENTER);
        layoutSinistraSospensione.add(new Span(profiloSegnalato.getNome()), durata, dettagli);

        //layout destra
        VerticalLayout layoutDestraSospensione = new VerticalLayout();

        Button inviaSospensione = new Button("Invia Sospensione");
        inviaSospensione.addClickListener(e -> {
            try {
                SospensioneDTO sospensioneDTO = new SospensioneDTO(Integer.parseInt(durata.getValue()), dettagli.getValue());
                gestioneModerazioneController.inviaSospensione(sospensioneDTO, studenteSegnalato.getEmail());
            }catch (InvalidBanFormatException ex){
                new Notification("Dettagli e/o durata non validi",2000, Notification.Position.MIDDLE).open();
            }
            cardSospensione.close();
            UI.getCurrent().getPage().reload();
        });


        layoutDestraSospensione.setAlignItems(FlexComponent.Alignment.CENTER);
        layoutDestraSospensione.add(new Span(studenteSegnalato.getEmail()), inviaSospensione);


        layoutInfoSospensione.add(layoutSinistraSospensione,layoutDestraSospensione);
        return layoutInfoSospensione;
    }


    public HorizontalLayout infoAmmonimento(Notification cardAmmonimento, FotoDTO fotoSegnalata, ProfiloDTO profiloSegnalato, StudenteDTO studenteSegnalato){

        HorizontalLayout layoutInfoAmmonimento = new HorizontalLayout();

        //layout sinistra
        VerticalLayout layoutSinistraInfo = new VerticalLayout();
        layoutSinistraInfo.setAlignItems(FlexComponent.Alignment.CENTER);

        Select<String> motivazione = new Select<>();
        Motivazione[] motivaziones =  Motivazione.values();
        motivazione.setItems(motivaziones[0].toString(), motivaziones[1].toString(), motivaziones[2].toString(), motivaziones[3].toString(), motivaziones[4].toString());

        TextField dettagli = new TextField();
        dettagli.setPlaceholder("Dettagli");

        layoutSinistraInfo.add(new Span(profiloSegnalato.getNome()),
                motivazione, dettagli);


        //layout destra
        VerticalLayout layoutDestraInfo = new VerticalLayout();

        Button inviaAmmonimento = new Button("Invia Ammonimento");
        inviaAmmonimento.addClickListener(e -> {
            try {
                AmmonimentoDTO ammonimentoDTO = new AmmonimentoDTO(Motivazione.valueOf(motivazione.getValue()), motivazione.getValue());
                gestioneModerazioneController.inviaAmmonimento(ammonimentoDTO, moderatore.getEmail(), studenteSegnalato.getEmail(), fotoSegnalata);
            }catch(InvalidWarningFormatException ex1){
                new Notification("Motivazione e/o dettagli non validi",2000, Notification.Position.MIDDLE).open();
            }
            cardAmmonimento.close();
            UI.getCurrent().getPage().reload();

        });

        layoutDestraInfo.setAlignItems(FlexComponent.Alignment.CENTER);
        layoutDestraInfo.add(new Span(studenteSegnalato.getEmail()), inviaAmmonimento);


        layoutInfoAmmonimento.add(layoutSinistraInfo,layoutDestraInfo);
        return layoutInfoAmmonimento;
    }
}
