package com.sitevisit.smartfieldoperations.service;


import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import com.sitevisit.smartfieldoperations.entity.SiteVisit;

import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;


@Service
public class PdfService {


    public ByteArrayInputStream generateSiteVisitsPdf(
            List<SiteVisit> visits
    ) {


        Document document = new Document();

        ByteArrayOutputStream out =
                new ByteArrayOutputStream();


        try {


            PdfWriter.getInstance(
                    document,
                    out
            );


            document.open();


            Font titleFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            18
                    );


            Paragraph title =
                    new Paragraph(
                            "Scheduled Site Visits Report",
                            titleFont
                    );


            title.setAlignment(
                    Element.ALIGN_CENTER
            );


            document.add(title);

            document.add(
                    new Paragraph(" ")
            );


            PdfPTable table =
                    new PdfPTable(8);


            table.setWidthPercentage(100);


            table.addCell("Company");
            table.addCell("Email");
            table.addCell("Phone");
            table.addCell("Registration");
            table.addCell("Address");
            table.addCell("Date");
            table.addCell("Time");
            table.addCell("Status");



            for(SiteVisit visit : visits){


                table.addCell(
                        visit.getCompany().getName()
                );


                table.addCell(
                        visit.getCompany().getEmail()
                );


                table.addCell(
                        visit.getCompany().getPhone()
                );


                table.addCell(
                        visit.getCompany().getRegNumber()
                );


                table.addCell(
                        visit.getCompany().getAddress()
                );


                table.addCell(
                        visit.getVisitDate().toString()
                );


                table.addCell(
                        visit.getVisitTime().toString()
                );


                table.addCell(
                        visit.getStatus()
                );

            }


            document.add(table);


            document.close();


        } catch(Exception e){

            e.printStackTrace();

        }


        return new ByteArrayInputStream(
                out.toByteArray()
        );

    }

}