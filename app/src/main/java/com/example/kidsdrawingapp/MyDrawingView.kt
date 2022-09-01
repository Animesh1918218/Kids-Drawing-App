package com.example.kidsdrawingapp

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class MyDrawingView(context:Context,attributes:AttributeSet):View(context,attributes){
    private var mDrawpaint:Paint?=null
    private var mPath:CustomPath?=null
    private var mCanvasPaint:Paint?=null
    private var mCanvas:Canvas?=null
    private var mBitmap:Bitmap?=null
    private var color =Color.BLACK
    private var mBrushSize:Float = 0f
    private val myAllPaths =ArrayList<CustomPath>()
    init {
        setAllValues()
    }
    fun setAllValues(){
        mDrawpaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mDrawpaint?.color=color
        mDrawpaint?.style=Paint.Style.STROKE
        mDrawpaint?.strokeJoin=Paint.Join.ROUND
        mDrawpaint?.strokeCap=Paint.Cap.ROUND
        mCanvasPaint= Paint(Paint.DITHER_FLAG)
       // mBrushSize= 20f
        mPath=CustomPath(color,mBrushSize)

    }

   internal  inner class CustomPath(var color:Int,var brushWidth:Float):Path(){

   }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap!!)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawBitmap(mBitmap!!,0f,0f,mCanvasPaint)
        for(paths in myAllPaths){
            mDrawpaint?.color =paths.color
            mDrawpaint?.strokeWidth =paths.brushWidth
            canvas?.drawPath(paths,mDrawpaint!!)
        }
        if(!mPath!!.isEmpty){
           mDrawpaint?.color = mPath!!.color
            mDrawpaint?.strokeWidth =mPath!!.brushWidth
            canvas?.drawPath(mPath!!,mDrawpaint!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
       var touchx =event?.x
        var touchy=event?.y
        when(event?.action){
            MotionEvent.ACTION_DOWN->{
                mPath?.color=color
                mPath?.brushWidth= mBrushSize
                mPath?.reset()
                mPath?.moveTo(touchx!!,touchy!!)

            }
            MotionEvent.ACTION_MOVE->{
                mPath?.lineTo(touchx!!,touchy!!)

            }
            MotionEvent.ACTION_UP->{
                myAllPaths?.add(mPath!!)
              mPath =CustomPath(color,mBrushSize)

            }
            else ->{
                return false
            }
        }
        postInvalidate()
        return true
    }

    fun setNewBrushWidth(newSize : Float){
        mBrushSize =TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,newSize,resources.displayMetrics)
        mDrawpaint?.strokeWidth =mBrushSize
    }
    fun setNewColor(newcolor: String){
        color = Color.parseColor(newcolor)
    }
}