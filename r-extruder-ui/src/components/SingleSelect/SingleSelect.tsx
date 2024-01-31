import React from "react";
import "./SingleSelect.scss";
import { Select } from "@basf/react-components";

const selectData = [
  {
    id: "getId",
    name: "Get ID",
  },
  {
    id: "getDocument",
    name: "Get document",
  },
];

const SingleSelect = () => {
  return (
    <div className="custom-select">
      <Select
        buttonTitle="Submit"
        onConfirm={() => Promise.resolve()}
        selectData={selectData}
        isDisabled={true}
        isMultiple={false}
        title={"Select parameters"}
        autoSelect={true}
      />
    </div>
  );
};

export default SingleSelect;
