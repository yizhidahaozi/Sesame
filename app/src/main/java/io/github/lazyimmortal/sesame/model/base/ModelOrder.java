package io.github.lazyimmortal.sesame.model.base;

import io.github.lazyimmortal.sesame.data.Model;
import io.github.lazyimmortal.sesame.model.normal.answerAI.AnswerAI;
import io.github.lazyimmortal.sesame.model.normal.base.BaseModel;
import io.github.lazyimmortal.sesame.model.task.ancientTree.AncientTree;
import io.github.lazyimmortal.sesame.model.task.antCooperate.AntCooperate;
import io.github.lazyimmortal.sesame.model.task.antFarm.AntFarm;
import io.github.lazyimmortal.sesame.model.task.antForest.AntForestV2;
import io.github.lazyimmortal.sesame.model.task.antMember.AntMember;
import io.github.lazyimmortal.sesame.model.task.antOcean.AntOcean;
import io.github.lazyimmortal.sesame.model.task.antOrchard.AntOrchard;
import io.github.lazyimmortal.sesame.model.task.antSports.AntSports;
import io.github.lazyimmortal.sesame.model.task.antStall.AntStall;
import io.github.lazyimmortal.sesame.model.task.greenFinance.GreenFinance;
import io.github.lazyimmortal.sesame.model.task.reserve.Reserve;
import io.github.lazyimmortal.sesame.model.task.antDodo.AntDodo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ModelOrder {

    private static final Class<Model>[] array = new Class[]{
            BaseModel.class
            , AntForestV2.class
            , AntFarm.class
            , AntStall.class
            , AntOrchard.class
            , Reserve.class
            , AntDodo.class
            , AntOcean.class
            , AntCooperate.class
            , AncientTree.class
            , AntSports.class
            , AntMember.class
            , GreenFinance.class
            , AnswerAI.class
    };

    private static final List<Class<Model>> readOnlyClazzList = Collections.unmodifiableList(Arrays.asList(array));

    public static List<Class<Model>> getClazzList() {
        return readOnlyClazzList;
    }

}