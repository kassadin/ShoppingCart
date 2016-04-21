package com.github.kassadin.shoppingcart;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.kassadin.shoppingcart.entity.XcfResp;
import com.github.kassadin.shoppingcart.entity.XcfResp.ContentBean.CartItemsBean.CommodityBean.GoodsBean;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;
import java.io.InputStreamReader;

import butterknife.Bind;
import butterknife.ButterKnife;

public class XcfActivity extends AppCompatActivity {

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.listview) ListView mListview;
    @Bind(R.id.cb_all) CheckBox mCbAll;
    @Bind(R.id.total_price) TextView mTotalPrice;
    @Bind(R.id.total_price_final) TextView mTotalPriceFinal;
    @Bind(R.id.doit) Button mDoit;

    XcfResp mXcfResp;
    XcfAdapter mAdapter = new XcfAdapter();

    public static void start(Context context) {
        Intent starter = new Intent(context, XcfActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xcf);
        ButterKnife.bind(this);
        setUpToolbar();
        setUpListView();
        refresh();
    }

    private void setUpListView() {
        mListview.setAdapter(mAdapter);
    }

    private void refresh() {
        Gson gson = new Gson();
        try {
            InputStreamReader isr = new InputStreamReader(getResources().getAssets().open("xcf.txt"));
            XcfResp xcfResp = gson.fromJson(isr, XcfResp.class);
            if (xcfResp != null) {
                mXcfResp = xcfResp;
                mAdapter.notifyDataSetChanged();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setUpToolbar() {
        setSupportActionBar(mToolbar);
    }

    private GoodsBean getGoods(int position) {
        return mXcfResp.getContent().getCart_items().get(position).getCommodity().getGoods();
    }

    class XcfAdapter extends BaseAdapter {
        public static final int TYPE_SHOP = 0;
        public static final int TYPED_GOODS = 1;
        ImageLoader mImageLoader = ImageLoader.getInstance();

        @Override
        public int getCount() {
            return mXcfResp == null ? 0 : mXcfResp.getContent().getCount();
        }

        @Override
        public Object getItem(int position) {
            return mXcfResp.getContent().getCart_items().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int itemViewType = getItemViewType(position);
            GoodsBean goodsBean = getGoods(position);

            ShopVh shopVh = null;
            GoodsVh goodsVh = null;
            if (convertView == null) {
                switch (itemViewType) {
                    case TYPE_SHOP:
                        convertView = getLayoutInflater().inflate(R.layout.xcf_item_shop_header, parent, false);
                        shopVh = new ShopVh(convertView);
                        convertView.setTag(shopVh);
                        break;
                    case TYPED_GOODS:
                        convertView = getLayoutInflater().inflate(R.layout.xcf_item_normal, parent, false);
                        goodsVh = new GoodsVh(convertView);
                        convertView.setTag(goodsVh);
                        break;
                }
            } else {
                switch (itemViewType) {
                    case TYPE_SHOP:
                        shopVh = (ShopVh) convertView.getTag();
                        break;
                    case TYPED_GOODS:
                        goodsVh = (GoodsVh) convertView.getTag();
                        break;
                }

            }

            switch (itemViewType) {
                case TYPE_SHOP:
                    if (shopVh != null) {
                        shopVh.mShopTitle.setText(goodsBean.getShop().getName());
                    }
                    break;
                case TYPED_GOODS:
                    if (goodsVh != null) {
                        goodsVh.mItemName.setText(goodsBean.getName());
                        goodsVh.mItemPrice.setText(goodsBean.getName());
                        mImageLoader.displayImage(goodsBean.getMain_pic().getUrl(), goodsVh.mItemImg);
                    }

                    break;
            }

            return convertView;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return TYPE_SHOP;
            } else {
                if (getGoods(position - 1).getShop().getId() == getGoods(position).getShop().getId()) {
                    return TYPED_GOODS;
                } else {
                    return TYPE_SHOP;
                }
            }
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        class ShopVh {

            public @Bind(R.id.shop_cb_select_all) CheckBox mShopCbSelectAll;
            public @Bind(R.id.shop_title) TextView mShopTitle;

            public ShopVh(View view) {
                ButterKnife.bind(this, view);
            }
        }

        class GoodsVh {
            public @Bind(R.id.item_cb_select) CheckBox mItemCbSelect;
            public @Bind(R.id.item_img) ImageView mItemImg;
            public @Bind(R.id.item_name) TextView mItemName;
            public @Bind(R.id.item_price) TextView mItemPrice;

            public GoodsVh(View view) {
                ButterKnife.bind(this, view);
            }

        }

    }

}
