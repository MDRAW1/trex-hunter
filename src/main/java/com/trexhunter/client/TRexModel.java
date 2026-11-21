package com.trexhunter.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/** Modelo do T-Rex: corpo inclinado, cabeção, mandíbula móvel, cauda longa e bracinhos. */
public class TRexModel extends EntityModel<TRexRenderState> {
    private final ModelPart body;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart tail;
    private final ModelPart tail2;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart leftArm;
    private final ModelPart rightArm;

    public TRexModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.neck = this.body.getChild("neck");
        this.head = this.neck.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.tail = this.body.getChild("tail");
        this.tail2 = this.tail.getChild("tail2");
        this.leftArm = this.body.getChild("left_arm");
        this.rightArm = this.body.getChild("right_arm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Quadril fica em y=-48 (pés em y=0 => ~3 blocos de altura de quadril).
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-9.0F, -14.0F, -16.0F, 18.0F, 26.0F, 32.0F),
                PartPose.offsetAndRotation(0.0F, -50.0F, 0.0F, -0.12F, 0.0F, 0.0F));

        PartDefinition neck = body.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(0, 58)
                        .addBox(-7.0F, -10.0F, -14.0F, 14.0F, 18.0F, 16.0F),
                PartPose.offsetAndRotation(0.0F, -6.0F, -14.0F, 0.35F, 0.0F, 0.0F));

        PartDefinition head = neck.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(60, 58)
                        .addBox(-8.0F, -9.0F, -26.0F, 16.0F, 14.0F, 26.0F)
                        .texOffs(60, 98)
                        .addBox(-7.0F, -12.0F, -10.0F, 3.0F, 3.0F, 3.0F)   // olho esquerdo (osso)
                        .texOffs(60, 98)
                        .addBox(4.0F, -12.0F, -10.0F, 3.0F, 3.0F, 3.0F)    // olho direito (osso)
                        .texOffs(0, 100)
                        .addBox(-6.0F, 5.0F, -25.0F, 12.0F, 2.0F, 2.0F),   // dentes de cima
                PartPose.offsetAndRotation(0.0F, -6.0F, -12.0F, -0.45F, 0.0F, 0.0F));

        head.addOrReplaceChild("jaw",
                CubeListBuilder.create().texOffs(0, 108)
                        .addBox(-7.0F, 0.0F, -24.0F, 14.0F, 5.0F, 24.0F)
                        .texOffs(0, 100)
                        .addBox(-5.0F, -2.0F, -23.0F, 10.0F, 2.0F, 2.0F),  // dentes de baixo
                PartPose.offset(0.0F, 5.0F, -1.0F));

        PartDefinition tail = body.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(0, 140)
                        .addBox(-6.0F, -7.0F, 0.0F, 12.0F, 16.0F, 30.0F),
                PartPose.offsetAndRotation(0.0F, -4.0F, 14.0F, 0.2F, 0.0F, 0.0F));

        tail.addOrReplaceChild("tail2",
                CubeListBuilder.create().texOffs(0, 190)
                        .addBox(-4.0F, -5.0F, 0.0F, 8.0F, 10.0F, 30.0F),
                PartPose.offset(0.0F, 0.0F, 29.0F));

        body.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(100, 0)
                        .addBox(-1.5F, 0.0F, -2.0F, 3.0F, 12.0F, 4.0F),
                PartPose.offsetAndRotation(8.5F, 6.0F, -14.0F, 0.6F, 0.0F, -0.1F));
        body.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(100, 0).mirror()
                        .addBox(-1.5F, 0.0F, -2.0F, 3.0F, 12.0F, 4.0F),
                PartPose.offsetAndRotation(-8.5F, 6.0F, -14.0F, 0.6F, 0.0F, 0.1F));

        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(100, 20)
                        .addBox(-5.0F, 0.0F, -6.0F, 10.0F, 30.0F, 12.0F)
                        .texOffs(100, 66)
                        .addBox(-5.0F, 30.0F, -14.0F, 10.0F, 5.0F, 8.0F),
                PartPose.offset(7.5F, -35.0F, 2.0F));
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(100, 20).mirror()
                        .addBox(-5.0F, 0.0F, -6.0F, 10.0F, 30.0F, 12.0F)
                        .texOffs(100, 66)
                        .addBox(-5.0F, 30.0F, -14.0F, 10.0F, 5.0F, 8.0F),
                PartPose.offset(-7.5F, -35.0F, 2.0F));

        return LayerDefinition.create(mesh, 128, 256);
    }

    @Override
    public void setupAnim(TRexRenderState state) {
        super.setupAnim(state);

        float walk = state.walkAnimationPos;
        float speed = Math.min(state.walkAnimationSpeed, 1.0F);

        this.leftLeg.xRot = Mth.cos(walk * 0.5F) * 1.0F * speed;
        this.rightLeg.xRot = Mth.cos(walk * 0.5F + (float) Math.PI) * 1.0F * speed;

        this.body.y = -50.0F + Math.abs(Mth.cos(walk * 0.5F)) * 2.5F * speed;
        this.body.xRot = -0.12F + speed * 0.08F;

        this.tail.yRot = Mth.cos(walk * 0.25F) * 0.15F * speed + Mth.sin(state.ageInTicks * 0.04F) * 0.04F;
        this.tail2.yRot = Mth.cos(walk * 0.25F - 0.8F) * 0.25F * speed;

        this.head.yRot = state.yRot * ((float) Math.PI / 180F) * 0.5F;
        this.head.xRot = -0.45F + state.xRot * ((float) Math.PI / 180F) * 0.5F;

        // Rugido: cabeça pra cima, boca aberta e tremendo.
        if (state.roaring) {
            float shake = Mth.sin(state.ageInTicks * 2.2F) * 0.04F;
            this.head.xRot = -1.05F + shake;
            this.jaw.xRot = 0.75F + Math.abs(shake) * 3.0F;
            this.neck.xRot = 0.1F;
            this.body.xRot = -0.3F;
        } else {
            this.jaw.xRot = 0.08F + (Mth.sin(state.ageInTicks * 0.1F) + 1.0F) * 0.03F;
            this.neck.xRot = 0.35F;
        }

        float armTwitch = Mth.sin(state.ageInTicks * 0.3F) * 0.1F;
        this.leftArm.xRot = 0.6F + armTwitch;
        this.rightArm.xRot = 0.6F - armTwitch;
    }
}
