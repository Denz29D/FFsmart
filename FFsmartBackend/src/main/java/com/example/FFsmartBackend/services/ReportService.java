package com.example.FFsmartBackend.services;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import com.example.FFsmartBackend.models.Inventory;

@Service
public class ReportService {
    public void generateCsvReport(Writer writer,List<Inventory>inventoryList)throws IOException{
        try(CSVPrinter printer=new CSVPrinter(writer,CSVFormat.DEFAULT.withHeader("ID", "Item Name", "Quantity", "Type", "Expiry Date", "Threshold Quantity", "Fridge Location"))){
            for(Inventory item:inventoryList){
                List<String>data=List.of(item.getId(),item.getItemName(),String.valueOf(item.getQuantity()),item.getType(),item.getExpiryDate().toString(),String.valueOf(item.getThresholdQuantity()),item.getFridgeLocation());
                printer.printRecord(data);
            }
        }
    }

}
