/*
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.tudarmstadt.ukp.experiments.argumentation.convincingness.sampling;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Ivan Habernal
 */
public class AnnotatedArgumentPair
        extends ArgumentPair
{

    protected List<MTurkAssignment> mTurkAssignments = new ArrayList<>();
    private String goldLabel;

    public List<MTurkAssignment> getmTurkAssignments()
    {
        return mTurkAssignments;
    }

    /**
     * Creates a shallow copy of the parameter
     *
     * @param argumentPair parameter
     */
    public AnnotatedArgumentPair(
            ArgumentPair argumentPair)
    {
        super(argumentPair);
    }

    /**
     * Empty constructor - use with caution! :)
     */
    public AnnotatedArgumentPair()
    {
    }

    public void setGoldLabel(String goldLabel)
    {
        this.goldLabel = goldLabel;
    }

    public String getGoldLabel()
    {
        return goldLabel;
    }

    public String toStringSimple()
    {
        return arg1.getId() + ":" + arg2.getId() + " (" + goldLabel + ")";
    }

    @Override public String toString()
    {
        return "AnnotatedArgumentPair{" +
                "mTurkAssignments=" + mTurkAssignments +
                ", goldLabel='" + goldLabel + '\'' +
                '}';
    }

    public static class MTurkAssignment
    {

        private String turkID;
        private String hitID;
        private Date assignmentAcceptTime;
        private Date assignmentSubmitTime;
        private String value;
        private String reason;
        private String hitComment;
        private String assignmentId;
        private Integer turkRank;
        private Double turkCompetence;
        private String workerStance;

        public String getTurkID()
        {
            return turkID;
        }

        public void setTurkID(String turkID)
        {
            if (turkID == null) {
                throw new IllegalArgumentException("Parameter turkID cannot be null");
            }

            this.turkID = turkID;
        }

        public String getHitID()
        {
            return hitID;
        }

        public void setHitID(String hitID)
        {
            if (hitID == null) {
                throw new IllegalArgumentException("Parameter hitID cannot be null");
            }

            this.hitID = hitID;
        }

        public Date getAssignmentAcceptTime()
        {
            return assignmentAcceptTime;
        }

        public void setAssignmentAcceptTime(Date assignmentAcceptTime)
        {
            if (assignmentAcceptTime == null) {
                throw new IllegalArgumentException("Parameter assignmentAcceptTime cannot be null");
            }

            this.assignmentAcceptTime = assignmentAcceptTime;
        }

        public Date getAssignmentSubmitTime()
        {
            return assignmentSubmitTime;
        }

        public void setAssignmentSubmitTime(Date assignmentSubmitTime)
        {
            if (assignmentSubmitTime == null) {
                throw new IllegalArgumentException("Parameter assignmentSubmitTime cannot be null");
            }

            this.assignmentSubmitTime = assignmentSubmitTime;
        }

        public String getValue()
        {
            return value;
        }

        public void setValue(String value)
        {
            if (value == null) {
                throw new IllegalArgumentException("Parameter value cannot be null");
            }

            this.value = value;
        }

        public String getReason()
        {
            return reason;
        }

        public void setReason(String reason)
        {
            if (reason == null) {
                throw new IllegalArgumentException("Parameter reason cannot be null");
            }

            this.reason = reason;
        }

        public String getHitComment()
        {
            return hitComment;
        }

        public void setHitComment(String hitComment)
        {
            this.hitComment = hitComment;
        }

        public void setAssignmentId(String assignmentId)
        {
            if (assignmentId == null) {
                throw new IllegalArgumentException("Parameter assignmentId cannot be null");
            }
            this.assignmentId = assignmentId;
        }

        public String getAssignmentId()
        {
            return assignmentId;
        }

        public void setTurkRank(Integer turkRank)
        {
            this.turkRank = turkRank;
        }

        public Integer getTurkRank()
        {
            return turkRank;
        }

        public void setTurkCompetence(Double turkCompetence)
        {
            if (turkCompetence == null) {
                throw new IllegalArgumentException("Parameter turkCompetence cannot be null");
            }
            this.turkCompetence = turkCompetence;
        }

        public Double getTurkCompetence()
        {
            return turkCompetence;
        }

        public void setWorkerStance(String workerStance)
        {
            this.workerStance = workerStance;
        }

        public String getWorkerStance()
        {
            return workerStance;
        }

        @Override public String toString()
        {
            return "MTurkAssignment{" +
                    "turkID='" + turkID + '\'' +
                    ", hitID='" + hitID + '\'' +
                    ", assignmentAcceptTime=" + assignmentAcceptTime +
                    ", assignmentSubmitTime=" + assignmentSubmitTime +
                    ", value='" + value + '\'' +
                    ", reason='" + reason + '\'' +
                    ", hitComment='" + hitComment + '\'' +
                    ", assignmentId='" + assignmentId + '\'' +
                    ", turkRank=" + turkRank +
                    ", turkCompetence=" + turkCompetence +
                    ", workerStance='" + workerStance + '\'' +
                    '}';
        }
    }

}
