package com.example.gui.prototipoautocomplete;


import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.TextView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.data.DataBufferUtils;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class AutocompleteAdapter extends RecyclerView.Adapter<AutocompleteAdapter.PlaceViewHolder> implements Filterable
{

    private ArrayList<AutocompletePrediction> lstResultados;
    private GoogleApiClient mGoogleApiClient;
    private LatLngBounds bounds;
    private AutocompleteFilter filtro;
    private Context context;


    public AutocompleteAdapter(Context context, GoogleApiClient mGoogleApiClient, LatLngBounds bounds, AutocompleteFilter filtro){
        this.context = context;
        this.mGoogleApiClient = mGoogleApiClient;
        this.bounds = bounds;
        this.filtro = filtro;

    }

    public void setBounds(LatLngBounds bounds){
        this.bounds = bounds;
    }

    @Override
    public Filter getFilter(){
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint){
                FilterResults results = new FilterResults();

                if(constraint != null){
                    lstResultados = getAutocomplete(constraint);
                    if(lstResultados != null){
                        results.values = lstResultados;
                        results.count = lstResultados.size();
                    }
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results){
                if(results != null && results.count > 0)
                    notifyDataSetChanged();
            }

            @Override
            public CharSequence convertResultToString(Object resultado){
                if(resultado instanceof AutocompletePrediction){
                    return ((AutocompletePrediction) resultado).getFullText(null);
                }else{
                    return super.convertResultToString(resultado);
                }

            }


        };

    }

    private ArrayList<AutocompletePrediction> getAutocomplete(CharSequence constraint){
        if(mGoogleApiClient.isConnected()){
            Log.i("getAutoComplete", "Query iniciando para:" +constraint);
            PendingResult<AutocompletePredictionBuffer> resultados =
                    Places.GeoDataApi
                            .getAutocompletePredictions(mGoogleApiClient, constraint.toString(), bounds,filtro);
            AutocompletePredictionBuffer autocompletePredictions = resultados.await(60, TimeUnit.SECONDS);

            final Status status = autocompletePredictions.getStatus();

            if(!status.isSuccess()){
                Toast.makeText(context, "Erro ao conectar com a API" + status.toString()
                        ,Toast.LENGTH_SHORT).show();
                autocompletePredictions.release();
                return null;

            }

            Log.i("getAutoComplete", "Query finalizada, resultados:"+autocompletePredictions.getCount());
            return DataBufferUtils.freezeAndClose(autocompletePredictions);





        }

        Log.e("Api Client", "Não foi possível conectar a GoogleApiCliente");
        return null;

    }



    public class PlaceViewHolder extends RecyclerView.ViewHolder{
        public TextView textView;
        public CardView cardView;

        public PlaceViewHolder(View view){
            super(view);
            textView = (TextView) view.findViewById(R.id.txtPlace);
        }
    }

    @Override
    public PlaceViewHolder onCreateViewHolder(ViewGroup viewgroup,int i){
       View convertView = LayoutInflater.from(viewgroup.getContext())
               .inflate(R.layout.item,viewgroup, false);
        return new PlaceViewHolder(convertView);

    }

    @Override
    public void onBindViewHolder(PlaceViewHolder viewHolder, final int i){

        viewHolder.textView.setText(lstResultados.get(i).getFullText(null));
    }

    @Override
    public int getItemCount(){
        if(lstResultados != null){
            return lstResultados.size();
        }else {
            return 0;
        }
    }
}
